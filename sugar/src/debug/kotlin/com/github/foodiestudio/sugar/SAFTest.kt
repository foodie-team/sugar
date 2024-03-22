package com.github.foodiestudio.sugar

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ListItem
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

@OptIn(ExperimentalMaterialApi::class, ExperimentalSugarApi::class)
@Composable
internal fun SAFTest(modifier: Modifier = Modifier, viewModel: SampleViewModel) {
    var selectedFolder: Uri? by remember {
        mutableStateOf(null)
    }
    val openFolderLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) {
            selectedFolder = it
        }
    var selectedFile: Uri? by remember {
        mutableStateOf(null)
    }
    val openFileLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) {
            selectedFile = it
        }
    Column(modifier) {
        ListItem(modifier = Modifier.clickable {
            openFolderLauncher.launch(null)
        }, text = {
            Text(text = "Document Folder Path Test")
        }, secondaryText = {
            Text(text = "Path: ${selectedFolder?.run(viewModel::fetchFilePathForDocumentUri)}")
        })
        ListItem(modifier = Modifier.clickable {
            openFileLauncher.launch(arrayOf("image/*"))
        }, text = {
            Text(text = "Document File Path Test")
        }, secondaryText = {
            Text(text = "Path: ${selectedFile?.run(viewModel::fetchFilePathForDocumentUri)}")
        })
    }
}