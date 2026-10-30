<div align="center">

#  Trabalho Prático – Sistemas Operacionais

### Comunicação e Sincronização de Processos e Threads

**Pontifícia Universidade Católica de Minas Gerais (PUC Minas)**
Engenharia de Software · Instituto de Ciências Exatas e Informática (ICEI)
Disciplina: Sistemas Operacionais · Prof. Diego Silva Caldeira Rocha

![Java](https://img.shields.io/badge/Java-17%2B-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Semáforos](https://img.shields.io/badge/Sincroniza%C3%A7%C3%A3o-Sem%C3%A1foros-blue?style=for-the-badge)
![Problemas](https://img.shields.io/badge/Problemas%20cl%C3%A1ssicos-6-success?style=for-the-badge)

</div>

---

##  Sobre o projeto

Implementação em **Java** de **6 problemas clássicos de sincronização**, usando **threads** e **semáforos** (`java.util.concurrent.Semaphore`) para controlar o acesso a recursos compartilhados e evitar **deadlocks**.

Cada problema tem o seu próprio `Main` e existe também um **Main geral** com um menu interativo, onde você escolhe qual problema deseja executar. Se preferir, também é possível executar o `Main` de cada problema separadamente.

Cada execução termina com uma seção de **validação** que informa `OK` ou `ERRO`, comprovando que a sincronização funcionou.

---

##  Vídeos

Os vídeos de explicação e validação estão disponíveis neste link (também registrado em [`Videos/Link.md`](Videos/Link.md)):

https://drive.google.com/drive/folders/1LHBTZUZua-vTxBMmGT6k8T2pzeCzd1vB?usp=sharing

Eles explicam cada problema clássico: modelagem, threads, recursos compartilhados, regiões críticas, semáforos, estratégia contra deadlock e a execução com a validação.

---

##  Escopo em relação ao enunciado

- **Parte I – item (a) Produtor/Consumidor:** exercício de editar, compilar e executar o código de exemplo do enunciado. Ele **não** faz parte deste repositório.
- **Parte II – itens (b) a (g):** os 6 problemas abaixo foram implementados com threads e semáforos.

---

##  Problemas implementados

| # | Problema | Pasta |
|:-:|----------|-------|
| 1 |  Barbeiro Dorminhoco | `barbeiroDominhoco` |
| 2 |  Leitores e Escritores | `leitorEscritor` |
| 3 |  Fumantes | `fumantes` |
| 4 |  Filósofos | `filosofos` |
| 5 |  Jantar dos Selvagens | `selvagens` |
| 6 |  Três Robôs (Bart, Lisa e Maggie) | `tresrobos` |

---

##  Estrutura

```text
trabalhoPraticoSO-main/
├── MainGeral.java                  ← menu para escolher o problema
├── Codigos/
│   ├── barbeiroDominhoco/Main.java
│   ├── leitorEscritor/Main.java
│   ├── fumantes/Main.java
│   ├── filosofos/Main.java
│   ├── selvagens/Main.java
│   └── tresrobos/Main.java
├── Videos/
│   └── Link.md                     ← link dos vídeos de explicação e validação
└── README.md
```

> Os arquivos usam `package trabalhoPraticoSO...`; ao compilar com `-d out`, o `javac` cria a estrutura de pacotes dentro de `out/` automaticamente.

---

##  Como executar

### Pré-requisito

☕ **JDK 17 ou superior** instalado. Confira com:

```bash
java -version
javac -version
```

### Opção 1 – Menu geral (recomendado)

Na **raiz do repositório** (a pasta que contém `MainGeral.java` e `Codigos/`):

```bash
# Linux / macOS / Git Bash
javac -d out MainGeral.java Codigos/*/Main.java
java -cp out trabalhoPraticoSO.MainGeral
```

```powershell
# Windows (PowerShell)
javac -d out MainGeral.java (Get-ChildItem Codigos -Recurse -Filter Main.java).FullName
java -cp out trabalhoPraticoSO.MainGeral
```

Menu exibido:

```text
==========================================
  PROBLEMAS CLASSICOS DE SINCRONIZACAO
==========================================
 1 - Barbeiro Dorminhoco
 2 - Leitores e Escritores
 3 - Fumantes
 4 - Filosofos
 5 - Jantar dos Selvagens
 6 - Tres Robos (Bart, Lisa, Maggie)
 0 - Sair
```

### Opção 2 – Executar um problema isolado

```bash
java -cp out trabalhoPraticoSO.Codigos.filosofos.Main
```

Troque `filosofos` por qualquer pasta da tabela acima (é necessário ter compilado antes, como na Opção 1).

---

##  Modelagem dos problemas

| Problema | Threads | Recurso / região crítica | Semáforos (valor inicial) | Estratégia anti-deadlock |
|----------|---------|--------------------------|---------------------------|--------------------------|
|  **Barbeiro** | 1 barbeiro + 10 clientes (3 cadeiras de espera) | `waiting` (cadeiras ocupadas) | `customers`(0), `barbers`(0), `mutex`(1) | `mutex` sempre liberado antes de bloquear em `barbers` |
|  **Leitores/Escritores** | 3 leitores + 2 escritores | vetor `M` e contador `rc` | `db`(1), `mutex`(1) | Sem espera circular; 1º leitor trava `db` e o último libera |
|  **Fumantes** | 1 representante + 3 fumantes | mesa (2 ingredientes) | `agente`(1), `fumante[3]`(0) | Só o representante decide quem acorda |
|  **Filósofos** | 5 filósofos | 5 hashis | `hashi[5]`(1), `mesa`(4) | No máximo 4 disputando hashis ao mesmo tempo |
|  **Selvagens** | 5 selvagens + 1 cozinheiro | caldeirão (`porcoes`) | `mutex`(1), `caldeiraoVazio`(0), `caldeiraoCheio`(0) | Cozinheiro nunca depende do `mutex` |
|  **Três Robôs** | Bart, Lisa, Maggie | ordem dos movimentos | `semBart`(1), `semLisa1`(0), `semMaggie`(0), `semLisa2`(0) | Cadeia circular com uma única permissão, sem *busy wait* |

>  Sequência dos robôs: **Bart → Lisa → Maggie → Lisa → (repete)**
>
>  Em Leitores/Escritores a prioridade é dos leitores, o que pode causar *starvation* de escritores.

---

##  Validação

Ao final de cada execução aparece a seção `--- VALIDACAO ---`:

| Problema | O que é verificado |
|----------|--------------------|
|  Barbeiro | Atendidos + desistentes = total de clientes, e sala de espera vazia no fim |
|  Leitores/Escritores | Nenhuma leitura inconsistente (vetor sempre com valores iguais); nenhum leitor e escritor, nem dois escritores, ao mesmo tempo na região crítica |
|  Fumantes | Um cigarro por rodada, sempre com o fumante correto |
|  Filósofos | Nenhum par de vizinhos come ao mesmo tempo; todos completam as refeições |
|  Selvagens | Porções servidas = esperado; ninguém se serve com o caldeirão vazio |
|  Três Robôs | Histórico de `move()` igual ao padrão esperado |

Exemplo de saída:

```text
--- VALIDACAO ---
Refeicoes: [3, 3, 3, 3, 3] | Total=15 / 15
OK: nenhum par de vizinhos comeu junto, todos comeram, sem deadlock.
```

---

##  Integrantes

| Nome | Contribuição |
|------|--------------|
|  Gabriel dos Santos Silva Coelho | Decidi fazer sozinho, sem grupo |
