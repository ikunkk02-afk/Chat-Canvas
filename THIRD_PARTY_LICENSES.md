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

## owo-lib

- Component: `io.wispforest:owo-lib`
- Version: 0.12.15.4+1.21
- Copyright: WispForest contributors
- License: MIT
- Source: https://github.com/wisp-forest/owo-lib

owo-lib is a required runtime dependency. It is not bundled in the Chat Canvas
JAR — players must install it separately.

## Fabric API

- Component: `net.fabricmc.fabric-api:fabric-api`
- Version: 0.116.14+1.21.1
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
- Version: 11.0.4
- Copyright: TerraformersMC contributors
- License: MIT
- Source: https://github.com/TerraformersMC/ModMenu

Mod Menu is an optional dependency providing an in-game mod list and
configuration entry point. It is not bundled in the Chat Canvas JAR.
