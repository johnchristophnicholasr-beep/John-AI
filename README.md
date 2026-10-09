# John AI Android App

A native Android chat app that calls the OpenAI Chat Completions API. Includes chat history for the current app session, API/model settings, and speech-to-text input.

## Build APK on GitHub Actions (phone-friendly)
1. Create a GitHub repository named `JohnAI`.
2. Upload the contents of this ZIP to the repository root (the `.github` folder must be included).
3. Open **Actions** → **Build John AI APK** → **Run workflow**.
4. Wait for the workflow to finish successfully.
5. Open the completed run, scroll to **Artifacts**, and download `John-AI-debug-APK`.
6. Extract the downloaded ZIP and install `app-debug.apk`.

## API setup and cost
- Create an API key from the OpenAI platform. ChatGPT subscriptions and API billing are separate.
- Add a payment method or available API credits if required by your account. API usage is billed by model and token usage; check current pricing before use.
- In John AI, tap **Settings**, paste the API key, and choose a model available to your API account (default: `gpt-4o-mini`).
- Never publish the API key in source code, screenshots, public repositories, or messages. This starter app stores it locally on the device; a production app should use a secure backend proxy so the key cannot be extracted from the APK.
- The selected model name is only a default. It does not guarantee that the account has access to that model.

## Current scope
This is a starter version, not a full ChatGPT clone. It has online text chat, session conversation context, settings, and speech recognition input. Image understanding, persistent chat history, streaming, sign-in, and spoken AI replies are not implemented yet.
