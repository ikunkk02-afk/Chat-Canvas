# Third-Party Licenses

## Vosk API

- Component: `com.alphacephei:vosk`
- Version: 0.3.45
- Copyright: Alpha Cephei Inc.
- License: Apache License 2.0
- Source: https://github.com/alphacep/vosk-api

Vosk is included in the Chat Canvas release JAR as a Fabric nested JAR. Its
transitive JNA 5.7.0 dependency is excluded; Chat Canvas uses the JNA version
provided by Minecraft.

## Chinese Vosk model

- Model: `vosk-model-small-cn-0.22`
- Language: Chinese
- License: Apache License 2.0
- Source: https://alphacephei.com/vosk/models
- Download: https://alphacephei.com/vosk/models/vosk-model-small-cn-0.22.zip
- SHA-256: `3AF8B0E7E0F835AE9D414CE5DF580237A3CFB08D586C9FBBB0F7FF29AD5B14BA`

The model is not included in the Chat Canvas JAR. It is downloaded only after
the player explicitly confirms installation.
