# CineFlix

O CineFlix é um aplicativo Android desenvolvido como projeto acadêmico da disciplina de desenvolvimento de aplicativos móveis. O projeto foi construído de forma incremental ao longo dos módulos da disciplina, começando com uma proposta simples de pesquisa de filmes e evoluindo até incorporar persistência de dados, banco SQLite, comunicação por SMS e e-mail, WebView, serviços web, mapas e geolocalização.

O aplicativo começou como um protótipo no MIT App Inventor e, posteriormente, foi recriado no Android Studio utilizando Kotlin e XML. Essa mudança permitiu organizar melhor o código e aplicar de forma mais completa os recursos estudados durante o curso.

## Autor

Alex Magalhães Santos

## Visão geral do projeto

A proposta do CineFlix é reunir em um único aplicativo recursos úteis para quem deseja pesquisar e organizar informações sobre filmes. O usuário pode buscar títulos, consultar detalhes, salvar favoritos, explorar uma galeria, compartilhar recomendações por SMS ou e-mail e localizar cinemas próximos utilizando mapas e localização do dispositivo.

O projeto foi desenvolvido com foco no usuário final. Não existe uma interface administrativa, pois o CineFlix não realiza cadastro manual de filmes ou gerenciamento de usuários. Os filmes são obtidos de um catálogo local e também da API do The Movie Database (TMDB).

Entre as principais funcionalidades da versão final estão:

- Pesquisa de filmes por título e gênero;
- Pesquisa automática on-line no TMDB;
- Catálogo local em JSON;
- Tela inicial com filmes em destaque;
- Tela de detalhes com pôster, ano, gênero, nota e sinopse;
- Lista de favoritos em SQLite;
- Histórico de pesquisas;
- SharedPreferences;
- Arquivos internos e externos;
- Galeria com GridView e ImageSwitcher;
- Menu de opções e menu de contexto;
- Página de ajuda com WebView;
- Compartilhamento por SMS e e-mail;
- Recebimento de SMS com BroadcastReceiver;
- Google Maps;
- Localização atual e monitoramento de localização;
- Geocodificação e geocodificação reversa;
- Pesquisa de cinemas próximos;
- Comunicação HTTP;
- Processamento de dados JSON;
- Versionamento com Git e GitHub.

## Problema que o CineFlix procura resolver

Existem muitas fontes diferentes para pesquisar filmes, consultar informações, organizar favoritos e localizar cinemas. A ideia do CineFlix é reunir parte dessas tarefas em uma única experiência simples.

O usuário pode pesquisar um filme e encontrar informações básicas sem precisar navegar por diferentes páginas. Também pode manter seus favoritos salvos, compartilhar uma recomendação com outra pessoa e utilizar sua localização para procurar cinemas próximos.

Durante o desenvolvimento, procurei integrar cada novo recurso ao objetivo do aplicativo. Por isso, o mapa foi utilizado para localizar cinemas, o SMS e o e-mail foram utilizados para compartilhar recomendações e o WebView foi utilizado para criar uma área de ajuda do próprio CineFlix.

## Plataforma e tecnologias principais

A plataforma escolhida foi Android. A versão atual foi desenvolvida no Android Studio utilizando Kotlin e layouts em XML.

O projeto também utiliza recursos como RecyclerView, GridView, ImageSwitcher, WebView, SharedPreferences, SQLite, Intents, BroadcastReceiver, SmsManager, Google Maps, serviços de localização e comunicação HTTP.

## Estrutura geral de navegação

O fluxo principal do aplicativo pode ser representado de forma simplificada assim:

```text
Tela inicial
   |
   |-- Pesquisa de filmes
   |       |
   |       --> Detalhes do filme
   |                |
   |                |-- Favoritar
   |                |-- Recomendações
   |                --> Compartilhar filme
   |
   |-- Galeria de filmes
   |
   |-- Favoritos
   |
   |-- Cinemas próximos / mapa
   |
   |-- Sobre / Ajuda
   |
   --> Configurações
```

# Evolução do projeto por módulo

## Módulo 1: Dando vida ao CineFlix

No primeiro módulo foi definida a ideia que daria origem ao CineFlix. A proposta foi criar um aplicativo relacionado a filmes, inspirado na experiência de plataformas de streaming, mas com foco acadêmico e sem a intenção de reproduzir uma plataforma comercial.

O problema escolhido foi a necessidade de consultar informações sobre filmes de forma simples e concentrada em um único aplicativo. A partir disso, foram definidos os dados que deveriam aparecer para o usuário, como título, ano, gênero, avaliação, sinopse e pôster.

A plataforma escolhida foi Android. Nesse momento inicial também foi definida a ideia de uma interface voltada ao usuário final, sem necessidade de uma área administrativa.

O design inicial foi pensado com uma tela principal de pesquisa e uma área de resultado para apresentar as informações do filme encontrado. Esse esquema serviu como base para as versões seguintes.

As primeiras funcionalidades planejadas foram a pesquisa de filmes, apresentação das informações principais e navegação simples entre as telas.

## Módulo 2: Refinando a proposta e a experiência do usuário

No segundo módulo a ideia principal do CineFlix foi mantida, mas a organização do projeto foi refinada.

A descrição do aplicativo, o problema que ele pretendia resolver e a escolha da plataforma continuaram os mesmos. O principal avanço dessa fase foi pensar melhor na organização da interface e na experiência do usuário.

A pesquisa continuou sendo o centro da aplicação, mas o projeto começou a considerar uma navegação mais organizada entre tela inicial, resultados e detalhes do filme. Também foi reforçada a decisão de manter o aplicativo voltado apenas ao usuário final.

Essa etapa foi importante para amadurecer a estrutura antes da implementação de recursos mais específicos dos módulos seguintes.

## Módulo 3: Estruturando a interface e a entrada de dados

Os conceitos do Módulo 3 foram incorporados à versão atual do CineFlix no Android Studio.

As interfaces passaram a ser estruturadas em XML. A tela principal possui um `EditText` para entrada do termo pesquisado e o código Kotlin recupera o valor digitado para filtrar os filmes e iniciar pesquisas.

Também foram implementados eventos de clique em diferentes pontos do aplicativo, como seleção de filmes, botões de navegação e atalhos da tela inicial.

A tela de detalhes utiliza `NestedScrollView`, permitindo que sinopse, botões e recomendações possam ser visualizados mesmo quando o conteúdo ultrapassa a altura da tela.

Na versão atual, a pesquisa também foi aprimorada. Enquanto o usuário digita, o CineFlix procura imediatamente no catálogo local. Depois de uma pequena pausa na digitação, uma pesquisa on-line é executada automaticamente no TMDB. O usuário ainda pode confirmar a busca manualmente pelo teclado caso queira realizar a consulta imediatamente.

Com isso, os principais conceitos do módulo ficaram representados através de XML, EditText, recuperação de dados digitados, eventos de interação e conteúdo com rolagem.

## Módulo 4: Tornando o aplicativo mais visual e interativo

O Módulo 4 foi responsável por ampliar os componentes visuais e as formas de interação do CineFlix.

O `ImageView` é utilizado para exibir pôsteres na tela inicial, nos resultados, na tela de detalhes e em outras partes do aplicativo.

Também foi criada a `GaleriaActivity`. Nessa tela, os filmes são apresentados em um `GridView`, formando uma galeria visual. Ao selecionar um filme, o pôster pode ser exibido em um `ImageSwitcher` e o usuário consegue navegar entre as imagens pelos botões Anterior e Próximo, com transições simples.

O menu de contexto foi incorporado à galeria. Ao pressionar e segurar um filme, o usuário pode acessar ações relacionadas ao item selecionado, como abrir os detalhes ou adicionar e remover o filme dos favoritos.

O CineFlix também possui um menu de opções global na tela principal. Esse menu permite acessar Favoritos, Galeria de filmes, Cinemas próximos, Sobre/Ajuda e Configurações.

Para evitar repetição de código foi criada a classe `HelperMethods`. Ela centraliza métodos reutilizáveis, como normalização de textos para pesquisa e conversão de valores de dp para pixels.

Outro recurso implementado foi o `WebView`. Foi criada a `AjudaActivity`, que carrega o arquivo local `assets/ajuda.html` dentro do próprio aplicativo. Essa página explica o objetivo do CineFlix e suas principais funcionalidades sem abrir um navegador externo.

Durante essa etapa também foi realizada uma melhoria visual na tela inicial. O nome CineFlix passou a receber maior destaque, com identidade visual própria, e a Home passou a apresentar quatro filmes em destaque em uma grade 2 x 2, reduzindo o espaço vazio que existia na versão anterior.

## Módulo 5: Persistência de dados e armazenamento

O Módulo 5 representou uma das maiores evoluções do projeto. Foi nessa fase que o CineFlix foi recriado no Android Studio em Kotlin e passou a utilizar recursos mais completos de persistência.

O aplicativo utiliza `SharedPreferences` através da classe `PreferencesManager`. Um exemplo é a última pesquisa realizada pelo usuário, que permanece salva e pode ser reaproveitada posteriormente.

O histórico de pesquisas é gerenciado pela classe `HistoricoManager`, utilizando armazenamento interno. As pesquisas confirmadas podem ser gravadas em arquivo e recuperadas posteriormente.

O aplicativo também trabalha com armazenamento externo através da exportação de favoritos para um arquivo CSV. O arquivo pode conter título, gênero, ano, nota e sinopse dos filmes armazenados.

O catálogo local está localizado em:

```text
app/src/main/res/raw/movies.json
```

A classe `MovieRepository` acessa esse recurso através de `openRawResource()` e transforma o conteúdo JSON em objetos utilizados pelo aplicativo.

Os favoritos são persistidos em SQLite. A classe `DatabaseHelper` estende `SQLiteOpenHelper` e é responsável pelo banco:

```text
cineflix.db
```

A tabela de favoritos armazena informações como ID, título, gênero, ano, nota, sinopse e URL do pôster.

Essa estrutura permite que os filmes favoritos permaneçam disponíveis mesmo depois que o aplicativo é fechado.

O Módulo 5 também consolidou o uso de múltiplas Activities e Intents para navegar entre diferentes partes do CineFlix e compartilhar informações, como o ID do filme selecionado.

## Módulo 6: Comunicação por SMS e e-mail

No Módulo 6 foram adicionados recursos de comunicação. Para que essas funcionalidades fizessem sentido dentro do CineFlix, foi criada uma área de compartilhamento de recomendações de filmes.

Na `DetalhesActivity`, o usuário pode escolher a opção Compartilhar filme. O aplicativo abre a `ComunicacaoActivity` e prepara automaticamente uma mensagem utilizando informações do filme selecionado.

Exemplo:

```text
Recomendação do CineFlix: Nexus Zero (2023) • Nota: 8.4. Vale a pena conferir!
```

### Envio direto de SMS

O CineFlix utiliza `SmsManager` para realizar o envio direto de uma mensagem SMS. Como essa operação utiliza uma permissão sensível do Android, a permissão `SEND_SMS` é solicitada ao usuário quando o recurso é utilizado.

### SMS através do aplicativo de mensagens

Também foi implementado o envio através do aplicativo de mensagens instalado no dispositivo. Nesse caso é utilizada uma `Intent`, que abre o aplicativo de SMS com o número e a mensagem preparados para o usuário.

### Recebimento de SMS

Para o recebimento de mensagens foi criado o `SmsReceiver`, que estende `BroadcastReceiver`.

Quando um SMS chega ao dispositivo e a permissão `RECEIVE_SMS` foi autorizada, o receiver recupera o remetente e o conteúdo da mensagem. O `SmsRecebidoManager` armazena o último SMS recebido e a `ComunicacaoActivity` apresenta essas informações na tela.

Durante os testes no Android Emulator foi possível simular o recebimento de SMS e confirmar a exibição do número do remetente, data, horário e conteúdo da mensagem.

### Envio de e-mail

Também foi criada uma opção para compartilhar a recomendação por e-mail. O usuário informa o endereço do destinatário e o CineFlix abre um aplicativo de e-mail compatível por meio de uma `Intent`, preenchendo assunto e mensagem automaticamente.

Dessa forma, os quatro requisitos de comunicação do módulo ficaram integrados ao objetivo principal do aplicativo.

## Módulo 7: Mapas, geolocalização e serviços web

No Módulo 7 o CineFlix passou a trabalhar com localização geográfica real, mapas e serviços externos.

Foi criada a `CinemasActivity`, responsável pela integração com o Google Maps SDK for Android.

A localização do usuário é obtida através do `FusedLocationProviderClient`. O aplicativo trabalha com as permissões `ACCESS_COARSE_LOCATION` e `ACCESS_FINE_LOCATION` e, quando autorizado, pode mostrar a localização atual no mapa.

Para obter e acompanhar a posição são utilizados recursos como:

```text
getCurrentLocation()
lastLocation
LocationRequest
LocationCallback
LocationResult
```

Enquanto a tela de cinemas está ativa, o CineFlix pode receber atualizações de posição. Quando a Activity deixa de ficar ativa, o monitoramento é interrompido para evitar solicitações desnecessárias.

### Interação com o mapa

O usuário pode tocar diretamente em uma região do mapa. O aplicativo captura latitude e longitude, adiciona um marcador, movimenta a câmera e tenta encontrar o endereço correspondente.

### Geocodificação

O campo de endereço permite que o usuário digite um local, por exemplo:

```text
Praça Sete, Belo Horizonte
```

O `Geocoder` transforma o texto informado em coordenadas e movimenta o mapa até a região encontrada.

### Geocodificação reversa

Também foi implementado o processo inverso. Ao selecionar um ponto no mapa, o CineFlix utiliza as coordenadas para tentar descobrir o endereço correspondente.

### Pesquisa de cinemas próximos

A funcionalidade principal do mapa é a pesquisa de cinemas próximos.

A classe `CinemaRepository` consulta dados do OpenStreetMap através da Overpass API. A requisição procura locais classificados como:

```text
amenity=cinema
```

São considerados elementos dos tipos `node`, `way` e `relation`.

A comunicação é realizada através de `HttpURLConnection` e a resposta é processada em JSON com `JSONObject` e `JSONArray`.

Cada resultado é convertido em um objeto `CinemaMapa`, que contém informações como nome, latitude, longitude, endereço e distância aproximada.

A distância entre o usuário e cada cinema é calculada com `Location.distanceBetween()`. Os resultados são ordenados do cinema mais próximo para o mais distante e apresentados como marcadores no mapa.

Essa implementação permitiu utilizar Google Maps, localização, geocodificação, HTTP e JSON em uma funcionalidade realmente relacionada ao tema do projeto.

## Módulo 8: Consolidação e documentação final

O Módulo 8 representa a consolidação de todo o trabalho desenvolvido durante o período letivo.

Nesta etapa o projeto foi revisado como um todo para verificar se os requisitos trabalhados durante os módulos estavam presentes na versão final do CineFlix.

Também foi realizada uma reorganização da documentação para explicar não apenas quais recursos existem, mas como o aplicativo evoluiu e por que cada funcionalidade foi incorporada.

O README passou a reunir a descrição do projeto, declaração do problema, plataforma, interface, funcionalidades, design, estrutura das telas e evolução dos oito módulos.

Além disso, o código permanece versionado no GitHub, permitindo acompanhar as alterações realizadas durante o desenvolvimento.

# Principais telas do aplicativo

## Tela inicial

A `MainActivity` é a tela principal do CineFlix.

Ela apresenta a identidade visual do aplicativo, campo de pesquisa, última pesquisa realizada, atalhos para recursos importantes e quatro filmes em destaque.

O `RecyclerView` utiliza um `GridLayoutManager` com duas colunas para organizar os destaques.

A pesquisa local acontece enquanto o usuário digita. Depois de aproximadamente 600 milissegundos sem novas alterações no texto, o CineFlix pode realizar automaticamente uma pesquisa on-line no TMDB.

Esse pequeno atraso evita realizar uma requisição para cada letra digitada.

Também existe uma proteção para impedir que uma resposta antiga substitua uma pesquisa mais recente. Se o usuário mudar o termo antes que a resposta anterior chegue, o resultado antigo não é apresentado.

## Tela de detalhes

A `DetalhesActivity` apresenta as informações completas do filme escolhido:

```text
Pôster
Título
Ano
Gêneros
Nota
Sinopse
Favoritos
Recomendações
Compartilhamento
```

O conteúdo utiliza `NestedScrollView` para se adaptar a telas menores ou sinopses maiores.

## Favoritos

A `FavoritosActivity` apresenta os filmes armazenados no banco SQLite.

Os favoritos permanecem salvos mesmo depois que o aplicativo é encerrado.

## Galeria de filmes

A `GaleriaActivity` utiliza `GridView` para exibir o catálogo em formato de galeria.

O `ImageSwitcher` permite alternar a imagem principal através dos botões Anterior e Próximo.

Ao pressionar e segurar um filme, o menu de contexto oferece ações relacionadas ao item selecionado.

## Compartilhar filme

A `ComunicacaoActivity` reúne os recursos do Módulo 6.

Ela permite enviar SMS diretamente, abrir o aplicativo de mensagens, abrir um aplicativo de e-mail, autorizar o recebimento de SMS e visualizar a última mensagem recebida.

## Cinemas próximos

A `CinemasActivity` reúne os recursos do Módulo 7.

Ela possui Google Maps, localização atual, monitoramento, pesquisa de endereço, geocodificação, geocodificação reversa e pesquisa de cinemas próximos.

## Sobre e Ajuda

A `AjudaActivity` utiliza um `WebView` para carregar o arquivo local:

```text
app/src/main/assets/ajuda.html
```

A página explica o objetivo do CineFlix e apresenta orientações sobre suas principais funcionalidades.

# Organização das principais classes

```text
MainActivity.kt
DetalhesActivity.kt
FavoritosActivity.kt
ConfiguracoesActivity.kt
GaleriaActivity.kt
AjudaActivity.kt
ComunicacaoActivity.kt
CinemasActivity.kt

Movie.kt
MovieAdapter.kt
HomeMovieAdapter.kt
GaleriaAdapter.kt
RecomendadoAdapter.kt

MovieRepository.kt
TmdbRepository.kt
CinemaRepository.kt
CinemaMapa.kt

DatabaseHelper.kt
PreferencesManager.kt
HistoricoManager.kt
HelperMethods.kt
SmsRecebidoManager.kt
SmsReceiver.kt
```

A organização foi feita de forma que diferentes responsabilidades não ficassem concentradas em uma única Activity.

`MainActivity` controla a tela inicial e a pesquisa.

`MovieRepository` gerencia o catálogo local e mantém os filmes obtidos on-line disponíveis para outras telas.

`TmdbRepository` é responsável pela pesquisa de filmes no TMDB.

`DatabaseHelper` gerencia o banco SQLite.

`PreferencesManager` centraliza as preferências armazenadas em SharedPreferences.

`HistoricoManager` gerencia o histórico salvo em arquivo interno.

`CinemaRepository` realiza a pesquisa de cinemas através da Overpass API.

`HelperMethods` reúne métodos reutilizáveis.

`SmsReceiver` recebe o broadcast de mensagens SMS e `SmsRecebidoManager` mantém as informações da última mensagem recebida.

# Tecnologias e recursos utilizados

O CineFlix utiliza atualmente:

```text
Android Studio
Kotlin
XML
Android SDK
View Binding
RecyclerView
GridLayoutManager
GridView
ImageView
ImageSwitcher
WebView
NestedScrollView
Intent
BroadcastReceiver
SmsManager
SharedPreferences
SQLite
SQLiteOpenHelper
JSON
JSONObject
JSONArray
HTTP
HttpURLConnection
TMDB API
Google Maps SDK for Android
Google Play Services Location
FusedLocationProviderClient
LocationRequest
LocationCallback
Geocoder
OpenStreetMap
Overpass API
Git
GitHub
```

# Persistência de dados

O CineFlix utiliza diferentes formas de persistência de acordo com o tipo de informação.

`SharedPreferences` é utilizado para dados simples, como a última pesquisa.

Arquivos internos são utilizados pelo histórico de pesquisas.

O catálogo local fica em `res/raw/movies.json`.

O SQLite é utilizado para armazenar os favoritos.

Também existe a possibilidade de exportar os favoritos em formato CSV.

# Serviços externos

## TMDB

A pesquisa de filmes pela internet utiliza a API do The Movie Database.

A classe responsável é:

```text
TmdbRepository
```

As requisições são executadas fora da thread principal para evitar travamentos da interface.

## OpenStreetMap e Overpass API

A pesquisa de cinemas utiliza dados do OpenStreetMap através da Overpass API.

A classe responsável é:

```text
CinemaRepository
```

## Google Maps

O mapa é exibido pelo Google Maps SDK for Android e a localização utiliza Google Play Services Location.

# Configuração das chaves de API

As chaves utilizadas pelo projeto não são gravadas diretamente no código-fonte publicado.

Elas devem ser configuradas no arquivo:

```text
local.properties
```

Exemplo:

```properties
TMDB_API_KEY=SUA_CHAVE_TMDB
MAPS_API_KEY=SUA_CHAVE_GOOGLE_MAPS
```

A chave do TMDB é disponibilizada através de `BuildConfig.TMDB_API_KEY`.

A chave do Google Maps é enviada ao `AndroidManifest.xml` através de um `manifestPlaceholder`.

O arquivo `local.properties` não deve ser publicado no GitHub.

# Permissões utilizadas

O projeto utiliza permissões de acordo com as funcionalidades implementadas:

```text
INTERNET
ACCESS_NETWORK_STATE
ACCESS_COARSE_LOCATION
ACCESS_FINE_LOCATION
SEND_SMS
RECEIVE_SMS
WRITE_EXTERNAL_STORAGE (compatibilidade com versões antigas do Android)
```

As permissões sensíveis são solicitadas quando o usuário tenta utilizar o recurso correspondente.

# Testes realizados durante o desenvolvimento

Durante o desenvolvimento foram realizados testes no Android Emulator para verificar as principais funcionalidades do projeto.

A localização do emulador foi simulada com ADB durante os testes do mapa.

Exemplo:

```powershell
& "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe" emu geo fix -43.9345 -19.9167
```

O recebimento de SMS também foi validado no emulador através de um SMS simulado:

```powershell
& "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe" emu sms send 31999999999 "Teste de recebimento SMS do CineFlix"
```

O teste confirmou que o `SmsReceiver` conseguiu capturar o remetente e o conteúdo da mensagem e que os dados foram apresentados na tela de comunicação.

# Como executar o projeto

## Requisitos

- Android Studio instalado;
- Android SDK configurado;
- Emulador ou dispositivo Android;
- Conexão com a internet para recursos on-line;
- Chave da API TMDB;
- Chave do Google Maps;
- Maps SDK for Android habilitado no Google Cloud.

## Passos

Clone o repositório:

```bash
git clone https://github.com/AlexLearnsTech/CineFlix.git
```

Abra o projeto no Android Studio e aguarde a sincronização do Gradle.

Configure as chaves no arquivo `local.properties`:

```properties
TMDB_API_KEY=SUA_CHAVE_TMDB
MAPS_API_KEY=SUA_CHAVE_GOOGLE_MAPS
```

Depois selecione um emulador ou dispositivo Android e execute o aplicativo.

Na primeira utilização de recursos como localização, envio direto de SMS ou recebimento de SMS, o Android poderá solicitar as permissões necessárias.

# Capturas de tela

As capturas de tela do projeto podem ser utilizadas para demonstrar visualmente a evolução e as principais funcionalidades do CineFlix.

Entre as imagens mais úteis para a documentação estão:

```text
Tela inicial
Pesquisa de filmes
Detalhes do filme
Galeria
Menu de contexto
Favoritos
Compartilhamento por SMS e e-mail
Recebimento de SMS
Sobre / Ajuda
Mapa
Geocodificação
Cinemas próximos
```

# Repositório

O projeto está disponível em:

https://github.com/AlexLearnsTech/CineFlix

O Git e o GitHub foram utilizados para versionar o código e manter o projeto atualizado durante o desenvolvimento.

## Observação sobre o GitHub Classroom

O enunciado da disciplina menciona a utilização do GitHub Classroom. Como não foi disponibilizado anteriormente um link específico para uma atividade ou sala, o projeto foi mantido em um repositório normal do GitHub com o código e a documentação necessários para avaliação.

# Possíveis melhorias futuras

Embora os objetivos acadêmicos do projeto tenham sido atendidos, ainda existem possibilidades de evolução, como autenticação de usuários, perfis, sincronização de favoritos em nuvem, trailers, paginação, recomendações personalizadas, rotas até cinemas, informações adicionais sobre estabelecimentos, testes automatizados e melhorias de acessibilidade.

# Considerações finais

O desenvolvimento do CineFlix permitiu acompanhar a evolução de um aplicativo desde sua ideia inicial até uma versão capaz de integrar vários recursos da plataforma Android.

Ao longo dos módulos, o projeto deixou de ser apenas uma tela de pesquisa de filmes e passou a trabalhar com persistência, banco de dados, arquivos, serviços externos, comunicação, mapas e localização.

Uma das principais preocupações durante o desenvolvimento foi evitar que os novos recursos parecessem funções isoladas adicionadas apenas para atender aos enunciados. Sempre que possível, procurei relacioná-los ao objetivo do aplicativo. A localização foi utilizada para encontrar cinemas, o SMS e o e-mail para compartilhar filmes, o WebView para apresentar ajuda e o SQLite para manter favoritos.

Além dos requisitos da disciplina, também foram realizadas melhorias de usabilidade, como a nova identidade visual da tela inicial, os filmes em destaque e a pesquisa automática no TMDB.

A versão final do CineFlix representa a integração prática dos principais conceitos trabalhados durante os oito módulos e mostra como diferentes recursos do Android podem funcionar juntos dentro de uma mesma aplicação.

# Finalidade acadêmica

O CineFlix foi desenvolvido exclusivamente para fins acadêmicos.

Os dados dos filmes pesquisados pela internet são fornecidos pela API do The Movie Database.

Os dados utilizados na pesquisa de cinemas são obtidos através do OpenStreetMap e da Overpass API.

O mapa é exibido através do Google Maps SDK for Android.

O projeto não possui vínculo oficial com Netflix, TMDB, Google ou OpenStreetMap.

Desenvolvido por Alex Magalhães Santos.

