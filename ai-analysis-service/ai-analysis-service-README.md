# AI Analysis Service

## Mục tiêu
Phân tích transcript bằng LLM.

## Trách nhiệm
- Sửa ngữ pháp
- Đánh giá từ vựng
- Đánh giá cấu trúc câu
- Sinh phản hồi học tập

## API
`POST /analysis/feedback`

## Input
```json
{
  "transcript":"I go to school yesterday.",
  "pronunciation":88,
  "clarity":90
}
```

## Output
```json
{
  "grammarCorrection":"I went to school yesterday.",
  "grammarExplanation":"Yesterday requires the past tense.",
  "vocabularyLevel":"Intermediate",
  "feedback":"Improve pronunciation of 'school'."
}
```
