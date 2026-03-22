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
가능한 한 최신 문법을 써 주세요. 예시로,

```java
try{
    doSomething();
} catch(Exception ignored) {}
```
대신 JAVA-25의

```java
try {
    doSomething();
} catch (Exception _) {}
```
를 이용하세요. 

PR 전 필수적으로 Build / Test를 진행하세요.
