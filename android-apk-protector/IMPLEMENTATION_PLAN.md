# APK Protector - Plano de Implementação Completo

## Visão Geral

Este documento detalha o plano completo para implementar um aplicativo Android que protege APKs externos gerando versões trial criptografadas.

## Arquitetura do Sistema

### 1. Componentes Principais

#### 1.1 Interface do Usuário (UI)
- **MainActivity**: Interface principal com Material Design 3
- **ProtectionViewModel**: Gerencia estado e lógica de negócio
- **Seleção de Arquivo**: ActivityResultContract para escolher APKs
- **Configuração**: Checkbox Owner/Trial e input de dias

#### 1.2 Núcleo de Proteção
- **ApkProtector**: Orquestrador principal do processo
- **ApkAnalyzer**: Análise e extração de informações do APK
- **PayloadGenerator**: Geração do payload criptografado PLDX
- **ApkPatcher**: Modificação e reempacotamento do APK
- **ApkSigner**: Assinatura do APK final

#### 1.3 Criptografia
- **CryptoManager**: Gerenciamento de chaves e criptografia
- **Algoritmos**: AES-128/CBC/PKCS5 + HMAC-SHA256
- **Derivação de Chave**: HMAC-SHA256(master_key, timestamp + packageName)

#### 1.4 Stub Runtime
- **ProtectedApp**: Application class para APKs protegidos
- **PayloadLoader**: Carregamento e descompressão de DEX
- **PayloadDecryptor**: Descriptografia e validação
- **DexClassLoader**: Carregamento dinâmico de classes

## Fluxo de Proteção Detalhado

### Fase 1: Análise do APK Original
```kotlin
1. Validar arquivo APK selecionado
2. Extrair AndroidManifest.xml (formato binário AXML)
3. Obter informações:
   - packageName
   - versionName/versionCode
   - Application class original
   - Permissões e componentes
4. Extrair todos os arquivos classes*.dex
5. Validar integridade dos DEX files
```

### Fase 2: Geração do Payload Criptografado
```kotlin
1. Comprimir DEX files em ZIP:
   - classes.dex, classes2.dex, etc.
   - Usar deflate ou ZSTD se disponível
2. Gerar chave de criptografia:
   - build_timestamp = System.currentTimeMillis()
   - key_data = timestamp_le64 + packageName_utf8
   - aes_key = HMAC-SHA256(master_key, key_data)[0:16]
3. Criptografar payload:
   - iv = SecureRandom(16 bytes)
   - encrypted = AES-128/CBC/PKCS5(compressed_dex, aes_key, iv)
4. Calcular HMAC de integridade:
   - hmac_data = header + encrypted_payload
   - hmac = HMAC-SHA256(aes_key, hmac_data)
5. Montar estrutura PLDX v1
```

### Fase 3: Modificação do APK
```kotlin
1. Criar APK temporário
2. Copiar recursos originais (exceto DEX e manifest):
   - res/, assets/, lib/, META-INF/ (exceto assinaturas)
3. Adicionar payload criptografado:
   - assets/payload.pldx
4. Substituir classes.dex pelo stub
5. Modificar AndroidManifest.xml:
   - android:name="com.apkprotector.app.stub.ProtectedApp"
   - <meta-data name="REAL_APP_CLASS" value="[original]" />
   - <meta-data name="PAYLOAD_ASSET" value="payload.pldx" />
```

### Fase 4: Assinatura e Finalização
```kotlin
1. Zipalign do APK modificado
2. Assinar com keystore:
   - Usar keystore debug padrão ou importada
   - Suporte a v1, v2, v3, v4 signature schemes
3. Verificar assinatura
4. Salvar em /Download/Protected/
```

## Estrutura Binária PLDX v1

### Layout do Header (64 bytes)
```
Offset | Size | Field              | Descrição
-------|------|--------------------|------------------
0x00   | 4    | magic              | 0x504C4458 ("PLDX")
0x04   | 4    | version            | 1
0x08   | 8    | expire_ts_ms       | Timestamp expiração (LE)
0x10   | 4    | flags              | Flags de licença
0x14   | 4    | original_size      | Tamanho original (LE)
0x18   | 4    | encrypted_size     | Tamanho criptografado (LE)
0x1C   | 16   | iv                 | IV para AES
0x2C   | 16   | reserved           | Reservado (zeros)
0x3C   | 32   | hmac_sha256        | HMAC de integridade
0x5C   | N    | encrypted_payload  | Payload criptografado
```

### Flags de Licença
```
0x00000001 - OWNER_FLAG   (sem expiração)
0x00000002 - PAID_FLAG    (versão paga)
0x00000004 - TRIAL_FLAG   (versão trial)
0x00000008 - ZSTD_FLAG    (compressão ZSTD)
```

## Runtime do Stub

### Inicialização do ProtectedApp
```kotlin
1. onCreate() chamado pelo Android
2. Ler meta-data PAYLOAD_ASSET e REAL_APP_CLASS
3. Carregar assets/payload.pldx
4. Validar magic e version
5. Verificar expiração trial:
   if (expire_ts_ms != 0 && now() > expire_ts_ms) {
       showTrialExpiredDialog()
       return
   }
6. Verificar HMAC de integridade
7. Descriptografar payload
8. Descomprimir DEX files
9. Salvar em codeCacheDir
10. Criar DexClassLoader
11. Instanciar Application original
12. Chamar onCreate() da app original
```

### Tratamento de Expiração
```kotlin
- Mostrar dialog simples: "Período de avaliação encerrado"
- Não permitir execução da app
- Opção de contato/upgrade (se aplicável)
```

## Implementações Críticas

### 1. Parser AXML (AndroidManifest.xml)
```kotlin
// Necessário para ler/modificar manifest binário
class AxmlParser {
    fun parseManifest(bytes: ByteArray): ManifestInfo
    fun updateApplicationClass(bytes: ByteArray, newClass: String): ByteArray
    fun addMetaData(bytes: ByteArray, key: String, value: String): ByteArray
}
```

### 2. Geração de Stub DEX
```kotlin
// Opções de implementação:
1. DEX pré-compilado em assets/
2. Geração dinâmica com dx/d8
3. Template DEX com substituição de strings
```

### 3. Assinatura APK Robusta
```kotlin
// Usar com.android.apksig library
val signerConfig = ApkSigner.SignerConfig.Builder(
    "CERT", privateKey, listOf(certificate)
).build()

val apkSigner = ApkSigner.Builder(listOf(signerConfig))
    .setInputApk(inputApk)
    .setOutputApk(outputApk)
    .setV1SigningEnabled(true)
    .setV2SigningEnabled(true)
    .setV3SigningEnabled(true)
    .build()

apkSigner.sign()
```

### 4. Keystore Management
```kotlin
// Opções de keystore:
1. Debug keystore automática
2. Importação de .jks/.keystore
3. Geração de nova keystore
4. Suporte a .pem + .pk8
```

## Melhorias de Produção

### 1. Segurança Avançada
- Anti-debugging measures
- Code obfuscation
- Root detection
- Tamper detection
- Certificate pinning

### 2. Compressão Otimizada
- ZSTD compression para payloads grandes
- Compressão diferencial
- Compressão por classes

### 3. Licenciamento Avançado
- Servidor de licenças
- Verificação online
- Hardware fingerprinting
- Licenças flutuantes

### 4. UI/UX Melhorada
- Drag & drop de APKs
- Batch processing
- Progress detalhado
- Logs de debug
- Configurações avançadas

## Limitações Conhecidas

### 1. Compatibilidade
- APKs com native libraries complexas
- APKs com proteções existentes
- APKs com verificações de integridade próprias

### 2. Performance
- APKs muito grandes (>100MB)
- Múltiplos DEX files (>10)
- Dispositivos com pouco armazenamento

### 3. Funcionalidade
- Não suporta App Bundles (.aab)
- Não suporta split APKs
- Limitado a APKs single-user

## Testes Recomendados

### 1. Testes Unitários
- CryptoManager
- PayloadGenerator
- PLDX format validation

### 2. Testes de Integração
- Fluxo completo de proteção
- Diferentes tipos de APK
- Cenários de erro

### 3. Testes de Dispositivo
- Diferentes versões Android
- Diferentes OEMs
- Cenários de baixa memória

## Considerações Legais

⚠️ **IMPORTANTE**: 
- Uso exclusivamente pessoal
- Não violar direitos autorais
- Não contornar proteções legítimas
- Respeitar termos de licença de software

## Cronograma de Desenvolvimento

### Fase 1 (Semana 1-2): Core
- [x] Estrutura básica do projeto
- [x] UI principal
- [x] CryptoManager
- [x] Modelos de dados

### Fase 2 (Semana 3-4): Proteção
- [ ] ApkAnalyzer com parser AXML
- [ ] PayloadGenerator completo
- [ ] ApkPatcher funcional
- [ ] Stub DEX generation

### Fase 3 (Semana 5-6): Runtime
- [ ] ProtectedApp completo
- [ ] PayloadDecryptor
- [ ] DexClassLoader integration
- [ ] Trial expiration handling

### Fase 4 (Semana 7-8): Finalização
- [ ] ApkSigner robusto
- [ ] Keystore management
- [ ] Testes extensivos
- [ ] Documentação final

Este plano fornece a base completa para implementar um sistema robusto de proteção de APKs para uso pessoal.
