# AtCoder-CF-Study-PR-Bot

GitHub Actions에서 주기적으로 실행되는 Kotlin + Spring Boot 배치 앱입니다. 저장소의 open pull request를 조회한 뒤 PR 제목과 변경 파일 경로를 검사하고, 규칙을 만족하면 squash merge합니다. 규칙을 위반하면 PR에 실패 사유 댓글을 남깁니다.

## PR 제목 규칙

PR 제목은 다음 형식이어야 합니다.

```text
대회이름 / AtCoder핸들
```

`/` 양쪽 공백은 있어도 되고 없어도 됩니다. AtCoder 핸들은 `[A-Za-z0-9_-]+` 형식만 허용합니다.

예:

```text
ABC350 / tourist
ABC350/tourist
ABC350 / tourist_123
```

## 파일 경로 규칙

변경 파일은 PR 제목에서 파싱한 `대회이름`과 `AtCoder핸들`을 사용해 다음 경로 아래에 있어야 합니다.

```text
대회이름/AtCoder핸들/**
```

예를 들어 PR 제목이 `ABC350 / tourist`라면 다음 경로는 허용됩니다.

```text
ABC350/tourist/A.kt
ABC350/tourist/src/Main.kt
```

다음 경로는 허용되지 않습니다.

```text
ABC350/other/A.kt
README.md
```

## 환경변수

| 변수 | 설명 | 기본값 |
| --- | --- | --- |
| `REPO_OWNER` | GitHub 저장소 owner | 없음 |
| `REPO_NAME` | GitHub 저장소 이름 | 없음 |
| `GITHUB_TOKEN` | GitHub API 토큰 | 없음 |
| `DRY_RUN` | 실제 댓글/머지/Discord 전송 없이 로그만 출력 | `true` |
| `DISCORD_ENABLED` | Discord webhook 전송 활성화 | `false` |
| `DISCORD_WEBHOOK_URL` | Discord webhook URL | 빈 값 |

## 로컬 실행

JDK 21이 필요합니다.

```bash
export REPO_OWNER=your-owner
export REPO_NAME=your-repo
export GITHUB_TOKEN=your-token
export DRY_RUN=true

./gradlew bootRun
```

Windows PowerShell에서는 다음처럼 실행할 수 있습니다.

```powershell
$env:REPO_OWNER="your-owner"
$env:REPO_NAME="your-repo"
$env:GITHUB_TOKEN="your-token"
$env:DRY_RUN="true"

./gradlew bootRun
```

## GitHub Actions 설정

워크플로는 `.github/workflows/pr-bot.yml`에 있습니다.

- 매주 금요일 19:00 KST 실행
- GitHub Actions cron 기준: `0 10 * * 5`
- `workflow_dispatch`로 수동 실행 가능
- 수동 실행 시 `dry_run` input 지원

권한은 다음이 필요합니다.

```yaml
permissions:
  contents: write
  pull-requests: write
  issues: write
```

## dry-run

`DRY_RUN=true`이면 실제 side effect를 수행하지 않습니다.

- PR 댓글 작성 안 함
- PR merge 안 함
- Discord webhook 전송 안 함

대신 로그에 `Would comment`, `Would merge`, `Would send Discord notification` 형태로 출력합니다. 기본값은 `true`입니다.

## Discord Webhook

Discord 알림은 선택 기능입니다. `DISCORD_ENABLED=true`이고 `DISCORD_WEBHOOK_URL` 값이 있을 때만 invalid PR 요약을 전송합니다. `DRY_RUN=true`이면 전송하지 않고 로그만 출력합니다.

## GitHub Copilot Automatic Code Review

Copilot automatic code review는 GitHub 저장소 설정에서 별도로 활성화해야 합니다. 이 봇은 Copilot 리뷰 결과를 직접 처리하지 않고, PR 제목/파일 경로 검증과 댓글/merge 처리만 담당합니다.
