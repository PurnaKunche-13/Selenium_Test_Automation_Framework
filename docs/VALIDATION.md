# Actual delivery validation — 2026-10-06

| Check | Actual result |
|---|---|
| Three dependency-free production helpers compiled with Java 17 | PASS, real javac module execution |
| ConfigReader profile/default/JVM checks | PASS: 8 assertions |
| Environment-variable/JVM precedence in separate process | PASS: 3 assertions |
| RetryBudget bounds/cleanup/concurrency | PASS: 6 assertions, 50 concurrent reservation requests |
| SafeDiagnostics metadata sanitization | PASS: 6 assertions |
| Java syntax | PASS: 33 project classes plus one component-check source parsed; no dependency/type checking |
| XML / JSON / workflow YAML | PASS: 11 XML and 8 JSON files parsed; YAML parsed; suite source references resolve |
| Maven dependency resolution / full compilation | BLOCKED: Maven executable unavailable, exit 127 |
| All/default, smoke, regression, UI, API, parallel, self-check, diagnostics, browser-self-check | BLOCKED: each Maven command attempted, exit 127; no tests started |
| Chrome/Firefox/Edge startup, screenshots, report rendering, live HTTP contracts, driver isolation | NOT EXECUTED: Maven and browser prerequisites unavailable |
| GitHub Actions | NOT EXECUTED: configuration parsed only |

23 assertions executed successfully on the three dependency-free helpers. No full framework PASS is claimed. Earlier feature checks produced the same Maven prerequisite failure; an attempted Maven distribution download timed out through the available proxy. This is an environment limitation, not a diagnosed application test failure. No suite was skipped to force a green outcome.

Exact final command output is in validation-output.txt. Reusable tools/ComponentChecks.java contains the component probes. On a JDK-equipped Windows machine, compile ConfigReader, RetryBudget, SafeDiagnostics and ComponentChecks with javac into target/component-check; run with classpath `target/component-check;src/test/resources`. The separate environment probe requires AUTOMATION_ENV=qa and AUTOMATION_EXPLICIT_WAIT=29 and argument `environment`.

## Required next execution

Use README commands from the folder containing pom.xml. Start with `mvn test-compile`, browser-free self-check and browser-self-check; then business suites. Inspect evidence and terminal/Surefire output, rather than assuming suite selection means success. Diagnostics is intentionally failing and must retain a nonzero result; verify its image, HTML attachment and cleanup assertions before recording EXPECTED FAILURE. Controlled inner retry fixtures are intentionally failing but their outer self-check should pass only when the expected outcomes are asserted.

Planned business invocation counts: regression/default/parallel 42, UI 35, API 7, smoke 5. Browser-free self-check plans 11 outer invocations and browser-self-check plans 3 sessions. These counts are derived from annotations/providers, not measured run counts. No timings, pass rate or coverage percentage were invented.
