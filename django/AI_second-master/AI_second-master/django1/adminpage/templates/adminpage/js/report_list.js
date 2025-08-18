document.addEventListener('DOMContentLoaded', function () {
    fetch(`f"{BASE_URL}/api/v1/admin/reports`)
        .then(response => response.json())
        .then(data => {
            const reportsList = document.getElementById('reportsList');

            if (!Array.isArray(data) || data.length === 0) {
                reportsList.innerHTML = '<div class="empty-message">등록된 신고 내역이 없습니다.</div>';
                return;
            }

            data.forEach((report, index) => {
                const statusClass = report.status === '처리대기' ? 'status-pending'
                                : report.status === '처리중' ? 'status-processing'
                                : 'status-completed';

                const reportHTML = `
                    <div class="report-item">
                        <div class="status-badge ${statusClass}">${report.status}</div>
                        
                        <div class="report-header">
                            <div class="report-title">${report.title || '신고 제목 없음'}</div>
                            <div class="report-date">${report.createdAt || '-'}</div>
                        </div>
                        
                        <div class="report-details">
                            <div class="report-field">• 신고자: ${report.reporterName}</div>
                            <div class="report-field">• 피신고자: ${report.reportedName}</div>
                            <div class="report-field">• 신고사유: ${report.reportCategoryName}</div>
                        </div>
                        
                        <div class="button-group">
                            <button class="btn btn-detail" onclick="showDetail(${index})">세부보기</button>
                            <button class="btn btn-accept" onclick="showAccept(${index})">승인</button>
                            <button class="btn btn-reject" onclick="showReject(${index})">반려</button>
                        </div>
                    </div>
                `;
                reportsList.insertAdjacentHTML('beforeend', reportHTML);
            });
        })
        .catch(error => {
            console.error('신고 목록 불러오기 실패:', error);
            document.getElementById('reportsList').innerHTML = '<div class="empty-message">신고 데이터를 불러오는 데 실패했습니다.</div>';
        });
});

// 버튼 핸들러 예시 (미구현 시 콘솔로 처리)
function showDetail(index) {
    console.log(`세부보기 클릭됨: ${index}`);
}

function showAccept(index) {
    console.log(`승인 클릭됨: ${index}`);
}

function showReject(index) {
    console.log(`반려 클릭됨: ${index}`);
}
