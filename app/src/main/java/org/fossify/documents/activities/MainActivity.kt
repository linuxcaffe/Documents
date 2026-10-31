package org.fossify.documents.activities

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toUri
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.fossify.commons.activities.BaseComposeActivity
import org.fossify.commons.compose.extensions.appLaunchedCompose
import org.fossify.commons.compose.extensions.enableEdgeToEdgeSimple
import org.fossify.commons.extensions.checkAppIconColor
import org.fossify.commons.extensions.getFilenameFromUri
import org.fossify.commons.extensions.getMimeTypeFromUri
import org.fossify.commons.extensions.hideKeyboard
import org.fossify.commons.extensions.showErrorToast
import org.fossify.commons.extensions.toast
import org.fossify.commons.helpers.LICENSE_COMMONMARK
import org.fossify.commons.helpers.LICENSE_COMMONS_CSV
import org.fossify.commons.helpers.LICENSE_JSOUP
import org.fossify.commons.helpers.LICENSE_MAMMOTH
import org.fossify.commons.helpers.LICENSE_PDF_VIEWER
import org.fossify.commons.models.FAQItem
import org.fossify.documents.BuildConfig
import org.fossify.documents.R
import org.fossify.documents.data.RenameOutcome
import org.fossify.documents.data.TransferMode
import org.fossify.documents.extensions.shareDocuments
import org.fossify.documents.extensions.startAboutActivity
import org.fossify.documents.models.DocumentEntry
import org.fossify.documents.models.DocumentKind
import org.fossify.documents.models.NewDocumentType
import org.fossify.documents.ui.screens.NewDocumentDialog
import org.fossify.documents.ui.screens.DocumentsMainActions
import org.fossify.documents.ui.screens.MainScreen
import org.fossify.documents.ui.theme.DocumentsAppThemeSurface
import org.fossify.documents.viewmodels.DocumentsHomeSection
import org.fossify.documents.viewmodels.DocumentsViewModel

@Suppress("TooManyFunctions")
class MainActivity : BaseComposeActivity() {
    private var hasResumed = false

    private val viewModel by lazy {
        ViewModelProvider(
            this,
            DocumentsViewModel.factory(application)
        )[DocumentsViewModel::class.java]
    }

    private val openDocumentLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode != RESULT_OK) {
                return@registerForActivityResult
            }

            val data = result.data ?: return@registerForActivityResult
            val uri = data.data ?: return@registerForActivityResult
            viewModel.prepareDocument(uri, data.flags) { canRead ->
                if (canRead) {
                    launchDocument(uri)
                } else {
                    toast(R.string.document_no_longer_available)
                }
            }
        }

    private val openFolderLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode != RESULT_OK) {
                return@registerForActivityResult
            }

            val data = result.data ?: return@registerForActivityResult
            val uri = data.data ?: return@registerForActivityResult
            viewModel.rememberFolder(uri, data.flags)
        }

    private var pendingTransfer: TransferMode? = null

    // Move to / Copy to, "Other folder…": the chosen folder is remembered too, so the chooser offers it next time
    private val transferFolderLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val mode = pendingTransfer ?: return@registerForActivityResult
            pendingTransfer = null
            val uri = result.data?.data?.takeIf { result.resultCode == RESULT_OK } ?: return@registerForActivityResult
            viewModel.rememberFolder(uri, result.data?.flags ?: 0)
            viewModel.transferSelectedDocuments(uri.toString(), mode) { transfer ->
                transferReport(mode, transfer)?.let { report -> toast(report) }
            }
        }

    private val createTextDocumentLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            handleCreatedDocument(result.resultCode, result.data, DocumentKind.TEXT)
        }

    private val createMarkdownDocumentLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            handleCreatedDocument(result.resultCode, result.data, DocumentKind.MARKDOWN)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appLaunchedCompose(appId = BuildConfig.APPLICATION_ID)
        checkAppIconColor()
        enableEdgeToEdgeSimple()
        setContent {
            DocumentsAppThemeSurface {
                val uiState = viewModel.uiState.collectAsStateWithLifecycle().value
                val pendingNewDocument = viewModel.pendingNewDocument.collectAsStateWithLifecycle().value
                MainScreen(
                    uiState = uiState,
                    actions = documentsMainActions(),
                )
                if (pendingNewDocument != null) {
                    NewDocumentDialog(
                        type = pendingNewDocument,
                        folderName = uiState.activeFolder?.name.orEmpty(),
                        onCreate = ::createDocumentInActiveFolder,
                        onChooseLocation = {
                            viewModel.dismissNewDocument()
                            pickNewDocumentLocation(pendingNewDocument)
                        },
                        onDismiss = viewModel::dismissNewDocument,
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (hasResumed) {
            viewModel.refreshVisibleFolder()
        } else {
            hasResumed = true
        }
    }

    private fun documentsMainActions() = DocumentsMainActions(
        openDocument = { openDocumentLauncher.launch(DocumentsViewModel.openDocumentIntent) },
        openFolder = { openFolderLauncher.launch(DocumentsViewModel.openFolderIntent) },
        newTextFile = { newDocument(NewDocumentType.TEXT) },
        newMarkdownFile = { newDocument(NewDocumentType.MARKDOWN) },
        openSettings = ::launchSettings,
        openAbout = ::launchAbout,
        clearRecentDocuments = viewModel::clearRecentDocuments,
        onQueryChange = viewModel.onQueryChange,
        onFilterSelected = viewModel::onFilterSelected,
        onSortSelected = { viewModel.updateDisplayOptions(sort = it) },
        onViewModeSelected = { viewModel.updateDisplayOptions(viewMode = it) },
        onBack = viewModel::navigateBack,
        onShowHome = { viewModel.showSection(DocumentsHomeSection.HOME) },
        onShowRecent = { viewModel.showSection(DocumentsHomeSection.RECENT) },
        onShowFolders = { viewModel.showSection(DocumentsHomeSection.FOLDERS) },
        onShowFavorites = { viewModel.showSection(DocumentsHomeSection.FAVORITES) },
        onBreadcrumbClick = viewModel::navigateToBreadcrumb,
        onRetryFolder = viewModel::refreshVisibleFolder,
        onFolderClick = { viewModel.showSection(DocumentsHomeSection.FOLDER, it) },
        onDocumentClick = { document ->
            viewModel.prepareDocument(document.uri.toUri()) { canRead ->
                if (canRead) {
                    launchDocument(document.uri.toUri(), document.kind)
                } else {
                    toast(R.string.document_no_longer_available)
                }
            }
        },
        onToggleDocumentSelection = viewModel::toggleDocumentSelection,
        onToggleFolderSelection = viewModel::toggleFolderSelection,
        onSelectAll = viewModel::selectAllVisibleItems,
        clearSelection = viewModel::clearSelection,
        onOpenWith = ::openWith,
        onShareDocuments = ::shareDocuments,
        onRemoveSelection = viewModel::removeSelectedItems,
        loadInfo = viewModel::documentInfo,
        onCopyText = { text -> copyToClipboard(text) },
        loadChildFolders = viewModel::childFolders,
        onTransferSelection = { folderUri, mode ->
            viewModel.transferSelectedDocuments(folderUri, mode) { transfer ->
                transferReport(mode, transfer)?.let { report -> toast(report) }
            }
        },
        onTransferToOtherFolder = { mode ->
            pendingTransfer = mode
            transferFolderLauncher.launch(DocumentsViewModel.openFolderIntent)
        },
        onNewFolder = { name ->
            viewModel.createFolder(name) { created ->
                if (!created) {
                    toast(R.string.documents_new_folder_failed)
                }
            }
        },
        onRenameSelection = { name ->
            viewModel.renameSelectedDocument(name) { outcome ->
                when (outcome) {
                    is RenameOutcome.Renamed -> Unit
                    RenameOutcome.NameTaken -> toast(R.string.rename_name_taken)
                    RenameOutcome.Failed -> toast(R.string.rename_failed)
                }
            }
        },
        onDeleteSelection = {
            viewModel.deleteSelectedDocuments { deleted, failed ->
                if (deleted > 0) {
                    toast(resources.getQuantityString(R.plurals.delete_files_done, deleted, deleted))
                }
                if (failed > 0) {
                    toast(resources.getQuantityString(R.plurals.delete_files_failed, failed, failed))
                }
            }
        },
        onToggleSelectedFavorites = viewModel::toggleSelectedFavorites,
    )

    private fun newDocument(type: NewDocumentType) {
        if (viewModel.canCreateInActiveFolder()) {
            viewModel.requestNewDocument(type)
        } else {
            pickNewDocumentLocation(type)
        }
    }

    private fun pickNewDocumentLocation(type: NewDocumentType) {
        val (launcher, title) = when (type) {
            NewDocumentType.TEXT -> createTextDocumentLauncher to R.string.untitled_text_file
            NewDocumentType.MARKDOWN -> createMarkdownDocumentLauncher to R.string.untitled_markdown_file
        }
        launcher.launch(
            DocumentsViewModel.createDocumentIntent(mimeType = type.mimeType, title = getString(title))
        )
    }

    private fun createDocumentInActiveFolder(name: String) {
        viewModel.createDocumentInActiveFolder(name) { uri, type ->
            if (uri != null) {
                launchDocument(uri, type.kind)
            } else {
                toast(R.string.new_file_failed)
            }
        }
    }

    private fun handleCreatedDocument(resultCode: Int, data: Intent?, kind: DocumentKind) {
        val uri = data?.data
        if (resultCode == RESULT_OK && uri != null) {
            viewModel.prepareDocument(uri, data.flags) { canRead ->
                if (canRead) {
                    launchDocument(uri, kind)
                } else {
                    toast(R.string.document_no_longer_available)
                }
            }
        }
    }

    private fun launchDocument(uri: Uri) {
        val kind = DocumentKind.fromName(getFilenameFromUri(uri), getMimeTypeFromUri(uri))
        launchDocument(uri, kind)
    }

    private fun launchDocument(uri: Uri, kind: DocumentKind) {
        hideKeyboard()
        try {
            val intent = documentViewerIntent(uri, kind)
            if (intent == null) {
                toast(R.string.unsupported_document_type)
            } else {
                startActivity(intent)
            }
        } catch (_: SecurityException) {
            toast(R.string.document_no_longer_available)
        }
    }

    private fun openWith(document: DocumentEntry) {
        hideKeyboard()
        viewModel.validateDocuments(listOf(document)) { readable, unavailableCount ->
            if (unavailableCount > 0 || readable.isEmpty()) {
                toast(R.string.document_no_longer_available)
                return@validateDocuments
            }

            val uri = document.uri.toUri()
            val mimeType = document.mimeType.ifBlank { "*/*" }
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, mimeType)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            launchExternalIntent(
                Intent.createChooser(intent, getString(org.fossify.commons.R.string.open_with)),
            )
        }
    }

    private fun shareDocuments(documents: List<DocumentEntry>) {
        hideKeyboard()
        viewModel.validateDocuments(documents) { readable, unavailableCount ->
            if (unavailableCount > 0) {
                toast(R.string.some_documents_no_longer_available)
            }
            if (readable.isEmpty()) {
                return@validateDocuments
            }

            val uris = readable.map { it.uri.toUri() }
            val mimeType = readable.map { it.mimeType }.filter { it.isNotBlank() }.distinct()
                .singleOrNull() ?: "*/*"
            shareDocuments(uris, mimeType)
        }
    }

    private fun launchSettings() {
        hideKeyboard()
        startActivity(Intent(this, SettingsActivity::class.java))
    }

    private fun launchAbout() {
        val licenses =
            LICENSE_PDF_VIEWER or
                LICENSE_MAMMOTH or
                LICENSE_COMMONMARK or
                LICENSE_JSOUP or
                LICENSE_COMMONS_CSV
        val faqItems = ArrayList<FAQItem>()
        if (resources.getBoolean(org.fossify.commons.R.bool.is_google_play_build)) {
            faqItems.add(
                FAQItem(
                    title = org.fossify.commons.R.string.faq_2_title_commons,
                    text = org.fossify.commons.R.string.faq_2_text_commons
                )
            )
            faqItems.add(
                FAQItem(
                    title = org.fossify.commons.R.string.faq_6_title_commons,
                    text = org.fossify.commons.R.string.faq_6_text_commons
                )
            )
        }

        startAboutActivity(
            appNameId = R.string.app_name,
            licenseMask = licenses,
            versionName = BuildConfig.VERSION_NAME,
            packageName = packageName,
            repositoryName = "Documents",
            faqItems = faqItems,
            showFAQBeforeMail = false
        )
    }
}

private fun MainActivity.launchExternalIntent(intent: Intent) {
    try {
        startActivity(intent)
    } catch (_: ActivityNotFoundException) {
        toast(org.fossify.commons.R.string.no_app_found)
    } catch (error: SecurityException) {
        showErrorToast(error)
    } catch (error: IllegalArgumentException) {
        showErrorToast(error)
    }
}
