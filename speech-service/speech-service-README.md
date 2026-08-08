# Speech Service

## Mục tiêu
Chuyển file âm thanh thành văn bản (Speech-to-Text).

## Trách nhiệm
- Nhận file âm thanh.
- Kiểm tra định dạng.
- Chuyển đổi định dạng nếu cần.
- Gọi dịch vụ Speech-to-Text.
- Trả về transcript.

## API
`POST /speech/transcribe`

## Input
- multipart/form-data
- file: audio.wav

## Output
```json
{
  "audioId":"A001",
  "transcript":"I want to improve my English."
}
```

## Không chịu trách nhiệm
- Chấm phát âm
- Sửa ngữ pháp
- Lưu lịch sử
