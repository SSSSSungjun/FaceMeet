package com.ssafy.facemeet.client.ui.profile.partner.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.ssafy.facemeet.client.R
import com.ssafy.facemeet.client.ui.profile.partner.PartnerProfileViewModel
import com.ssafy.facemeet.client.ui.theme.ChosunSeirf
import com.ssafy.facemeet.client.ui.theme.Roboto
import com.ssafy.facemeet.core.data.remote.dto.response.ReportCategoryResponse
import com.ssafy.facemeet.core.util.constant.CommonColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    onReportSubmit: (categoryId: Int, reason: String) -> Unit,
    viewModel: PartnerProfileViewModel = hiltViewModel()
) {
    if (!showDialog) return

    val categories by viewModel.categories.collectAsState()

    LaunchedEffect(showDialog) {
        viewModel.fetchCategories()
    }

    var expanded by remember { mutableStateOf(false) }
    var selectedCategory: ReportCategoryResponse? by remember { mutableStateOf(null) }
    var reason by remember { mutableStateOf("") }


    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White),
        ) {
            Column(
                modifier = Modifier
                    .padding(top = 32.dp, start = 24.dp, end = 24.dp, bottom = 24.dp)
                    .fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_siren), // 🚨 아이콘 리소스
                        contentDescription = "신고하기",
                        tint = CommonColor.Orange, // 빨간색
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "신고하기",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        fontFamily = ChosunSeirf,
                        color = CommonColor.Orange
                    )
                }


                Spacer(modifier = Modifier.height(30.dp))

                Text(
                    "신고 유형",
                    fontWeight = FontWeight.Medium,
                    fontFamily = Roboto,
                    color = CommonColor.Gray900
                )
                Spacer(modifier = Modifier.height(6.dp))

                // 드롭다운
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded },
                ) {
                    OutlinedTextField(
                        value = selectedCategory?.name ?: "",
                        onValueChange = {},
                        readOnly = true,
                        placeholder = {
                            Text(
                                "카테고리를 선택하세요",
                                color = CommonColor.Gray400,
                                fontFamily = Roboto
                            )
                        },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                        },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = CommonColor.Gray400,
                            focusedBorderColor = CommonColor.Brown500,
                        ),
                        shape = RoundedCornerShape(6.dp),
                        textStyle = TextStyle.Default.copy(fontFamily = Roboto)
                    )


                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier
                            .background(Color.White)
                            .exposedDropdownSize()

                    ) {
                        categories.forEachIndexed { index, category ->
                            DropdownMenuItem(
                                text = { Text(category.name) },
                                onClick = {
                                    selectedCategory = category
                                    expanded = false
                                }
                            )
                        }
                    }


                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    "상세 사유",
                    fontWeight = FontWeight.Medium,
                    fontFamily = Roboto,
                    color = CommonColor.Gray900
                )
                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    placeholder = {
                        Text(
                            "상세 내용을 입력해주세요",
                            color = CommonColor.Gray400,
                            fontFamily = Roboto
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = CommonColor.Gray400,

                        focusedBorderColor = CommonColor.Brown500
                    ),

                    shape = RoundedCornerShape(6.dp),
                    textStyle = TextStyle.Default.copy(
                        fontFamily = Roboto,
                        color = CommonColor.Gray900
                    )

                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("취소", color = CommonColor.Gray500)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    val selectedId = selectedCategory?.id
                    val enabled = selectedId != null && reason.isNotBlank()

                    TextButton(
                        onClick = {
                            onReportSubmit(selectedId!!, reason)
                            onDismiss()
                        },
                        enabled = enabled
                    ) {
                        Text(
                            "신고하기",
                            color = if (enabled) CommonColor.Orange else CommonColor.Gray400
                        )
                    }

                }
            }
        }
    }
}

