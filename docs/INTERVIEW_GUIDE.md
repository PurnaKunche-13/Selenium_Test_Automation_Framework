# Interview guide

## Two-minute explanation

I extended an existing Java 17 Selenium and API framework rather than rebuilding it. UI tests use Page Object Model: pages own locators and interactions, and TestNG tests own assertions. BaseTest creates a fresh configurable Chrome, Firefox or Edge driver for each UI invocation, with ThreadLocal isolation and teardown cleanup. Explicit waits synchronize on observable conditions; I removed nested waiting in product polling.

JSON providers supply multiple users, products, checkout customers and API payloads. The framework selects 42 business invocations across smoke, UI, API and regression suites. ConfigReader supports local, qa and stage profiles with JVM, environment and file precedence. Those profiles share public demo targets.

A TestNG retry analyzer permits one additional failed-business-test attempt by default. Reports keep failed attempts and mark recovered cases flaky. UI failure evidence includes a unique screenshot and sanitized metadata. API traces keep status, timing and response structure while removing payload values. Exception messages are omitted from shared reports to reduce accidental disclosure. REST Assured request creation remains in the API client, separate from assertions.

I added separate controlled retry checks, a three-browser-session isolation and wait check, and an intentional failure diagnostic that verifies screenshot/report/cleanup behavior. GitHub Actions is configured to compile, run framework gates and execute a headless smoke or regression suite. During delivery, 23 compiled component assertions passed, but Maven and browsers were unavailable. I therefore have not claimed a full build, live-suite pass rate or measured time savings.

## Thirty questions and answers

1. **Why preserve this architecture?**

   It already separated page actions, test assertions, drivers, API requests and utilities; focused edits avoided a rewrite.

2. **What is the execution flow?**

   TestNG selects cases; BaseTest starts a per-invocation UI driver; tests call pages; the listener records evidence; teardown quits and removes the driver.

3. **Why Page Object Model?**

   Locators and interactions live in pages, while tests express business intent and assertions.

4. **Why a shared header?**

   Cart navigation and badge behavior are reused instead of copying locators into every page.

5. **Why ThreadLocal drivers?**

   Each executing UI thread owns its driver; cleanup removes the slot to avoid reuse.

6. **How is parallel isolation checked?**

   A separate local-page suite synchronizes three real sessions and asserts unique session IDs and thread ownership; it is not yet executed.

7. **Which browsers are supported?**

   Chrome, Firefox and Edge are configured in DriverFactory with headed/headless options and Selenium Manager.

8. **Why avoid sleeps?**

   Explicit waits poll an observable condition and fail with a timeout when the condition never occurs.

9. **Which waits exist?**

   Visibility, presence, clickability, invisibility, URL change/fragment, exact title, text, visible collections, exact counts and custom conditions.

10. **What nested-wait issue was fixed?**

   ProductsPage polling previously called a helper that could wait again; polling now performs immediate lookups.

11. **How is configuration resolved?**

   JVM properties override AUTOMATION variables, then the selected profile, then defaults.

12. **Are QA and stage actual deployments?**

   No. They use different wait settings but inherit the public demo targets; the README states this.

13. **How are inputs validated?**

   ConfigReader rejects unknown profiles, blank settings, malformed booleans and invalid numeric limits.

14. **How does data-driven testing work?**

   Gson loads typed JSON records and TestNG providers supply rows to business tests.

15. **How many cases are planned?**

   42 business invocations: 35 UI and seven API; five smoke invocations. These are not execution results.

16. **How are test rows identified?**

   Case IDs appear in report names and record toString output; credentials are not included in the row label.

17. **How do retries work?**

   A TestNG transformer attaches an analyzer to business tests; a synchronized budget permits one additional attempt by default.

18. **Are skips retried?**

   Genuine skips are not. TestNG retry-marked skips represent a failed attempt and remain visible as failures in the report.

19. **How are retries bounded per row?**

   The key contains test instance, context, qualified method and parameter values; distinct JSON IDs distinguish current rows.

20. **Can retries hide defects?**

   They can, so recovered cases are labelled flaky and failed attempts retained; zero retries is available for diagnosis.

21. **How is retry integration tested?**

   A browser-free outer check runs controlled nested TestNG fixtures for recovery, exhaustion and genuine skips; it has not run here.

22. **What evidence is captured?**

   UI failures capture an image plus sanitized URL, browser, UTC time and case name before cleanup, with unique filenames.

23. **Why omit exception messages?**

   Messages can expose payloads or secrets; shared reports retain exception class and stack locations instead.

24. **What API evidence is safe?**

   Method, sanitized route, status, timing, content type and JSON shape; headers, cookies and payload values are omitted.

25. **Why keep API tests independent?**

   They use ApiClient and do not extend BaseTest, so API-only execution needs no browser.

26. **What API operations are tested?**

   GET list/detail/missing, two POST rows, PUT and DELETE against JSONPlaceholder, with status/body/content-type/field checks.

27. **Does JSONPlaceholder persist writes?**

   The framework treats responses as demo echoes and does not depend on later persistence of writes.

28. **How are reports parallel-safe?**

   Each result owns its node; shared report creation/flush and counters are synchronized or concurrent. Live rendering remains unvalidated.

29. **Why a separate diagnostic suite?**

   Its deliberate failure exercises listener, screenshot, report and cleanup without making normal regression intentionally fail.

30. **What actually passed during delivery?**

   23 dependency-free assertions executed on three compiled helpers, plus syntax/configuration checks. Maven compilation, TestNG, browsers, API and CI remain blocked or unexecuted.
