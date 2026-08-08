# Pronunciation Service

## Mục tiêu
Đánh giá chất lượng phát âm.

## Trách nhiệm
- Chấm Pronunciation Score
- Fluency
- Clarity
- Accuracy
- Phát hiện từ phát âm sai
- Phát hiện lỗi trọng âm

## API
`POST /pronunciation/evaluate`

## Input
```json
{
  "audioId":"A001",
  "audio":"audio.wav",
  "transcript":"I want to improve my English."
}
```

## Output
```json
{
  "pronunciation":91,
  "fluency":89,
  "clarity":90,
  "accuracy":92,
  "mistakes":[{"word":"English","problem":"Word stress"}]
}
```
