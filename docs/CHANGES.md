# Exact change manifest

Compared byte-for-byte with the extracted original ZIP; generated target files excluded.

## Created (16)

```text
docs/CHANGES.md
docs/PROJECT_REVIEW.md
src/test/java/com/purna/automation/api/ApiEvidence.java
src/test/java/com/purna/automation/api/SafeApiFilter.java
src/test/java/com/purna/automation/framework/BrowserFrameworkTest.java
src/test/java/com/purna/automation/framework/RetryIntegrationTest.java
src/test/java/com/purna/automation/listeners/RetryAnalyzer.java
src/test/java/com/purna/automation/listeners/RetryTransformer.java
src/test/java/com/purna/automation/utilities/RetryBudget.java
src/test/java/com/purna/automation/utilities/SafeDiagnostics.java
src/test/resources/config-qa.properties
src/test/resources/config-stage.properties
src/test/resources/testdata/customers.json
src/test/resources/testdata/posts.json
suites/browser-self-check.xml
tools/ComponentChecks.java
```

## Modified (39)

```text
.github/workflows/automation-tests.yml
README.md
docs/BUILD_STAGES.md
docs/INTERVIEW_GUIDE.md
docs/RESUME_BULLETS.md
docs/VALIDATION.md
docs/validation-output.txt
src/test/java/com/purna/automation/api/ApiClient.java
src/test/java/com/purna/automation/api/ApiTest.java
src/test/java/com/purna/automation/base/BaseTest.java
src/test/java/com/purna/automation/base/DriverFactory.java
src/test/java/com/purna/automation/framework/FailureEvidenceTest.java
src/test/java/com/purna/automation/framework/FrameworkSelfTest.java
src/test/java/com/purna/automation/listeners/ReportManager.java
src/test/java/com/purna/automation/listeners/TestListener.java
src/test/java/com/purna/automation/pages/CartPage.java
src/test/java/com/purna/automation/pages/CheckoutPage.java
src/test/java/com/purna/automation/pages/HeaderComponent.java
src/test/java/com/purna/automation/pages/ProductsPage.java
src/test/java/com/purna/automation/tests/CartTest.java
src/test/java/com/purna/automation/tests/CheckoutTest.java
src/test/java/com/purna/automation/tests/LoginTest.java
src/test/java/com/purna/automation/utilities/ConfigReader.java
src/test/java/com/purna/automation/utilities/JsonUtils.java
src/test/java/com/purna/automation/utilities/ScreenshotUtils.java
src/test/java/com/purna/automation/utilities/TestDataUtils.java
src/test/java/com/purna/automation/utilities/WaitUtils.java
src/test/resources/config.properties
src/test/resources/logback-test.xml
src/test/resources/testdata/loginData.json
src/test/resources/testdata/products.json
suites/api.xml
suites/diagnostics.xml
suites/parallel.xml
suites/regression.xml
suites/self-check.xml
suites/smoke.xml
suites/ui.xml
testng.xml
```

## Deleted

None.

## Implementation map

| Feature | Main files and mechanism | Reason |
|---|---|---|
| Edge/profiles | DriverFactory, ConfigReader, config*.properties | Configurable browser/profile coverage without changing tests |
| Waits/data | WaitUtils, ProductsPage, TestDataUtils, JSON and business tests | Avoid nested waits and broaden reusable positive inputs |
| Retry | RetryAnalyzer, RetryTransformer, RetryBudget, suite listener registrations | Bounded retries with per-row history and honest recovered status |
| Evidence | SafeDiagnostics, JsonUtils, ApiEvidence, SafeApiFilter, ScreenshotUtils, TestListener, ReportManager | Preserve useful failure metadata while omitting sensitive values |
| API contracts | ApiClient, ApiTest | Reuse request setup and assert required field types |
| Validation | FrameworkSelfTest, RetryIntegrationTest, BrowserFrameworkTest, FailureEvidenceTest, BaseTest | Add controlled infrastructure checks and evidence-before-cleanup verification |
| CI/docs | Workflow, README, docs, tools/ComponentChecks.java | Reproducible commands, framework gates and truthful validation |

POM dependencies, package names, original targets and existing test functionality were retained. No new dependency was added. Runtime validation boundaries are listed in VALIDATION.md.
