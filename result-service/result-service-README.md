# Result Service

## Mục tiêu
Điều phối quy trình và tổng hợp kết quả.

## Trách nhiệm
- Gọi Speech Service
- Gọi Pronunciation Service
- Gọi AI Analysis Service
- Tổng hợp kết quả
- Lưu CSDL
- Trả kết quả cho Frontend

## API
`POST /analysis/process`

## Luồng
1. Nhận audio.
2. Gọi Speech Service.
3. Gọi Pronunciation Service.
4. Gọi AI Analysis Service.
5. Gộp dữ liệu.
6. Lưu kết quả.
7. Trả JSON.
