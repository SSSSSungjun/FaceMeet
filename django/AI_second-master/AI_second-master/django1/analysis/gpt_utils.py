import openai
from django.conf import settings

client = openai.OpenAI(
    api_key=settings.OPENAI_API_KEY,
    base_url=settings.OPENAI_BASE_URL,
)

def get_gpt_response(prompt: str) -> str:
    response = client.chat.completions.create(
        model="gpt-4.1-nano",
        messages=[
            {"role": "system", "content": "당신은 친절한 AI 분석가입니다."},
            {"role": "user", "content": prompt},
        ],
        temperature=0.7,
        max_tokens=2048
    )
    return response.choices[0].message.content
