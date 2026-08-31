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

## sherpa-onnx

- Component: `com.k2fsa.sherpa.onnx:sherpa-onnx-jvm`
- Version: 1.13.4
- Copyright: k2-fsa contributors
- License: Apache License 2.0
- Source: https://github.com/k2-fsa/sherpa-onnx

The sherpa-onnx JVM bindings are included in the Chat Canvas release JAR as a
Fabric nested JAR. Platform-native libraries are not bundled; they are
downloaded at runtime from the official sherpa-onnx GitHub releases after the
player starts voice setup (SHA-256 verified). On iOS, the native runtime must
be supplied by the launcher through the sherpa runtime bridge.

## Silero VAD model

- Model: `silero_vad.int8.onnx`
- Copyright: Silero team
- License: MIT
- Source: https://github.com/snakers4/silero-vad
- Download: https://github.com/k2-fsa/sherpa-onnx/releases/download/asr-models/silero_vad.int8.onnx
- SHA-256: `C36D490AFF5AB924CA6C7AEEC4D8F6BD3D22DB6FA17611B9C5B17EAE58AC3A20`

Used for local voice activity detection. Downloaded at runtime and never
bundled in the Chat Canvas JAR.

## sherpa-onnx ASR models (runtime download)

The optional ASR models (streaming Zipformer Chinese, SenseVoice INT8,
Whisper Tiny INT8) are distributed through the
`csukuangfj/sherpa-onnx-*` Hugging Face repositories and are downloaded at
runtime after the player explicitly chooses a model (SHA-256 verified).
Each model repository carries its own license; see the respective Hugging
Face model page.

## owo-lib

- Component: `io.wispforest:owo-lib`
- Version: 0.12.21+1.21.5
- Copyright: WispForest contributors
- License: MIT
- Source: https://github.com/wisp-forest/owo-lib

owo-lib is a required runtime dependency. It is not bundled in the Chat Canvas
JAR — players must install it separately.

## Fabric API

- Component: `net.fabricmc.fabric-api:fabric-api`
- Version: 0.128.2+1.21.5
- Copyright: FabricMC contributors
- License: Apache License 2.0
- Source: https://github.com/FabricMC/fabric

Fabric API is a required runtime dependency. It is not bundled in the Chat
Canvas JAR — players must install it separately.

## Fabric Loader

- Component: `net.fabricmc:fabric-loader`
- Version: 0.19.3
- Copyright: FabricMC contributors
- License: Apache License 2.0
- Source: https://github.com/FabricMC/fabric-loader

Fabric Loader is a required runtime dependency. It is not bundled in the Chat
Canvas JAR.

## Mod Menu (optional)

- Component: `com.terraformersmc:modmenu`
- Version: 13.0.2
- Copyright: TerraformersMC contributors
- License: MIT
- Source: https://github.com/TerraformersMC/ModMenu

Mod Menu is an optional dependency providing an in-game mod list and
configuration entry point. It is not bundled in the Chat Canvas JAR.
