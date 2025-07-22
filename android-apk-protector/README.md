# APK Protector

Um aplicativo Android para proteger APKs externos gerando versões trial criptografadas para uso pessoal.

## Características

- **Proteção de APK**: Criptografa e protege APKs com sistema de trial/licença
- **Modo Owner**: Gera versões sem expiração para o proprietário
- **Modo Trial**: Gera versões com período de avaliação configurável
- **Criptografia Segura**: Usa AES-128 + HMAC-SHA256 para proteção
- **Assinatura APK**: Assina automaticamente os APKs protegidos
- **Interface Moderna**: UI Material Design 3

## Requisitos

- Android 7.0+ (API 24)
- Compatível até Android 15
- Permissões de armazenamento para ler/escrever APKs

## Como Usar

1. **Selecionar APK**: Toque em "Browse APK File" para escolher um APK
2. **Configurar Proteção**:
   - Marque "Owner" para versão sem expiração
   - Ou configure dias de trial (padrão: 14 dias)
3. **Proteger**: Toque em "Protect APK" para iniciar o processo
4. **Resultado**: APK protegido será salvo em `/Android/data/com.apkprotector.app/files/Protected/`

## Estrutura do Projeto

```
app/src/main/java/com/apkprotector/app/
├── crypto/          # Gerenciamento de criptografia
├── model/           # Modelos de dados
├── payload/         # Geração de payload criptografado
├── protector/       # Lógica principal de proteção
├── stub/            # Classes stub para APKs protegidos
├── utils/           # Utilitários (análise, patch, assinatura)
├── ui/              # Interface do usuário
└── viewmodel/       # ViewModels MVVM
```

## Formato PLDX v1

O payload criptografado usa o formato binário PLDX:

```
Offset | Size | Field
-------|------|-------
0x00   | 4    | Magic ("PLDX")
0x04   | 4    | Version (1)
0x08   | 8    | Expire Timestamp (ms)
0x10   | 4    | Flags
0x14   | 4    | Original Size
0x18   | 4    | Encrypted Size
0x1C   | 16   | IV
0x2C   | 16   | Reserved
0x3C   | 32   | HMAC-SHA256
0x5C   | N    | Encrypted Payload
```

## Flags

- `0x00000001`: Owner
- `0x00000002`: Paid
- `0x00000004`: Trial

## Segurança

- **Derivação de Chave**: `HMAC-SHA256(master_key, build_ts_ms_le64 || packageName_utf8)`
- **Criptografia**: AES-128/CBC/PKCS5Padding
- **Integridade**: HMAC-SHA256 sobre header + payload
- **IV**: 16 bytes aleatórios seguros

## Limitações Atuais

Esta é uma implementação de referência. Para uso em produção, seria necessário:

1. **Parser AXML**: Implementar parser completo para AndroidManifest.xml
2. **Stub DEX**: Compilar e incluir DEX stub real
3. **Assinatura Robusta**: Implementar assinatura APK completa
4. **Análise APK**: Parser completo de recursos e metadados
5. **Tratamento de Erros**: Melhor handling de casos edge

## Uso Responsável

⚠️ **IMPORTANTE**: Este aplicativo é destinado exclusivamente para uso pessoal. Use apenas para proteger seus próprios APKs. Respeite os direitos autorais e termos de licença de software de terceiros.

## Dependências

- AndroidX (Core, AppCompat, Material, ConstraintLayout, Lifecycle)
- APK Signing Library (`com.android.tools.build:apksig`)
- BouncyCastle Crypto (`org.bouncycastle:bcprov-jdk15on`)
- Apache Commons Compress
- ZSTD Compression (opcional)

## Compilação

```bash
./gradlew assembleDebug
```

## Licença

Este projeto é fornecido como está, para fins educacionais e de uso pessoal.
