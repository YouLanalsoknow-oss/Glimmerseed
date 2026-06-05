import logging

import httpx

from config import settings

logger = logging.getLogger(__name__)


async def chat(pet_name: str, pet_appearance: str, pet_personality: str, pet_color: str, user_message: str) -> str:
    system_prompt = (
        f"你是一只名叫{pet_name}的桌宠。外观特征：{pet_appearance}。"
        f"性格特点：{pet_personality}。颜色：{pet_color}。"
        "请以可爱、友好的方式与主人互动，回答要简短有趣。"
    )

    body = {
        "model": settings.siliconflow_model,
        "messages": [
            {"role": "system", "content": system_prompt},
            {"role": "user", "content": user_message},
        ],
        "temperature": 0.7,
        "max_tokens": 2000,
    }

    headers = {
        "Authorization": f"Bearer {settings.siliconflow_api_key}",
        "Content-Type": "application/json",
    }

    try:
        async with httpx.AsyncClient(timeout=30.0) as client:
            resp = await client.post(
                f"{settings.siliconflow_base_url}/chat/completions",
                json=body,
                headers=headers,
            )
            resp.raise_for_status()
            data = resp.json()
            reply = data["choices"][0]["message"]["content"]
            logger.info("AI reply received for pet '%s': %d chars", pet_name, len(reply))
            return reply
    except httpx.HTTPStatusError as e:
        logger.error("SiliconFlow HTTP error %s: %s", e.response.status_code, e.response.text)
    except Exception as e:
        logger.error("SiliconFlow API call failed: %s", e)

    return "抱歉，我暂时无法回答你的问题。"
