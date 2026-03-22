코드 규약
==========================

모든 코드는 [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html)를 따라 작성해야 합니다.

모든 커밋은 꼭 branch 를 파서 pull request 형식으로 main 에 merge 되어야합니다.
branch의 이름은 꼭 의미있는 이름으로 하세요. 예를 들어

```
codex/work/1 // this is bad
```

```
feat_user_auth // this is good
```

문법
==========================
프로젝트는 Java 25를 기준으로 작성합니다.

- 가능한 한 Java 25의 안정적인 문법과 표준 API를 우선 사용해 주세요.
- preview 또는 incubator 기능은 명시적인 합의 없이 도입하지 마세요.
- 최신 문법 사용보다 가독성과 유지보수성을 우선하세요.

예시로,

```java
try{
    doSomething();
} catch(Exception ignored) {}
```
처럼 의도가 불분명한 형태보다는,

```java
try {
    doSomething();
} catch (Exception e) {
    log.warn("Request failed", e);
}
```
처럼 현재 의도가 더 잘 드러나는 형태를 우선해 주세요.

PR 전 필수적으로 Build / Test를 진행하세요.

커밋 메세지 관련
==========================

커밋 메세지는 [Conventional Commits](https://www.conventionalcommits.org/en/v1.0.0/) 를 준용합니다. 
