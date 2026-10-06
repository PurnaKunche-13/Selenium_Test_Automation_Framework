# Baseline review and prioritized plan

Original archive SHA-256: d9038bf45c99c51565d5f8075fce4d127f8e7895aa350f73faed715adc9366ca. The extracted baseline was preserved; changes were made in a separate copy.

## Original structure (54 files)

```text
.github/workflows/automation-tests.yml
.gitignore
README.md
docs/BUILD_STAGES.md
docs/INTERVIEW_GUIDE.md
docs/RESUME_BULLETS.md
docs/SOURCES.md
docs/VALIDATION.md
docs/validation-output.txt
logs/.gitkeep
pom.xml
reports/.gitkeep
screenshots/.gitkeep
src/test/java/com/purna/automation/api/ApiClient.java
src/test/java/com/purna/automation/api/ApiEndpoints.java
src/test/java/com/purna/automation/api/ApiTest.java
src/test/java/com/purna/automation/base/BaseTest.java
src/test/java/com/purna/automation/base/DriverFactory.java
src/test/java/com/purna/automation/framework/FailureEvidenceTest.java
src/test/java/com/purna/automation/framework/FrameworkSelfTest.java
src/test/java/com/purna/automation/listeners/ReportManager.java
src/test/java/com/purna/automation/listeners/TestListener.java
src/test/java/com/purna/automation/pages/BasePage.java
src/test/java/com/purna/automation/pages/CartPage.java
src/test/java/com/purna/automation/pages/CheckoutPage.java
src/test/java/com/purna/automation/pages/HeaderComponent.java
src/test/java/com/purna/automation/pages/LoginPage.java
src/test/java/com/purna/automation/pages/ProductsPage.java
src/test/java/com/purna/automation/tests/CartTest.java
src/test/java/com/purna/automation/tests/CheckoutTest.java
src/test/java/com/purna/automation/tests/LoginTest.java
src/test/java/com/purna/automation/tests/ProductsTest.java
src/test/java/com/purna/automation/tests/RegressionTest.java
src/test/java/com/purna/automation/utilities/ConfigReader.java
src/test/java/com/purna/automation/utilities/JsonUtils.java
src/test/java/com/purna/automation/utilities/ScreenshotUtils.java
src/test/java/com/purna/automation/utilities/TestDataUtils.java
src/test/java/com/purna/automation/utilities/WaitUtils.java
src/test/resources/config.properties
src/test/resources/logback-test.xml
src/test/resources/testdata/checkoutErrors.json
src/test/resources/testdata/customer.json
src/test/resources/testdata/loginData.json
src/test/resources/testdata/post.json
src/test/resources/testdata/products.json
src/test/resources/testdata/sortModes.json
suites/api.xml
suites/diagnostics.xml
suites/parallel.xml
suites/regression.xml
suites/self-check.xml
suites/smoke.xml
suites/ui.xml
testng.xml
```

## Existing implementation

25 Java classes already implemented POM, reusable header, explicit waits, thread-local fresh Chrome/Firefox drivers, JSON-based negative login/checkout/sort cases, six REST Assured GET/POST/PUT/DELETE cases, Extent reporting, rolling logs, separate intentional diagnostics, local self-checks, parallel suite and Actions artifacts. Original suite selection planned 26 UI + six API invocations. API tests did not launch browsers. Dependencies were pinned and test-scoped.

| Area | Baseline assessment | Targeted action |
|---|---|---|
| Page objects, packages, targets, dependencies | Implemented; retained | Add action logs; fix nested product polling |
| Driver/configuration | Partial: Chrome/Firefox and override support | Add Edge and local/qa/stage profiles |
| Waits and JSON providers | Partial: existing working helpers and negative data | Expand contracts and reuse positive data rows |
| Retry policy | Missing | Add bounded per-case TestNG integration with visible history |
| Screenshots/report/logs | Implemented, partial metadata/safety | Unique filenames, sidecars, retry summaries and conservative diagnostics |
| API operations | Implemented, partial structural/evidence checks | Required fields and safe bounded request/response traces |
| Parallel isolation | Implemented design, live validation missing | Add separate three-session test |
| Diagnostic/self-checks | Implemented, incomplete integration proof | Strengthen evidence check; add retry fixtures and profile/safety checks |
| CI | Implemented, not executed | Add compile/framework gates, inputs, recovered-failure artifacts |

## Prioritized implementation plan

1. Preserve architecture and baseline; improve configuration/browser selection.
2. Expand explicit waits and reuse JSON data without duplicating working cases.
3. Add bounded retries and transparent attempt history.
4. Add conservative evidence capture and API structural assertions.
5. Add focused framework/diagnostic checks and CI gates.
6. Update documentation, record actual validation and deliver a new archive.

After each major feature, source parsing and available component checks were performed and Maven execution attempted. Maven/browser absence prevents dependency-backed verification; no failing test was disabled. See BUILD_STAGES and VALIDATION for the precise boundaries.
