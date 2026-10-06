# Official reference sources

Reviewed during preparation on 2026-10-06. Online documentation can change. These links support tool behavior and design choices, not a claim that the delivered tests passed.

- Selenium releases: https://www.selenium.dev/downloads/
- Selenium library installation: https://www.selenium.dev/documentation/webdriver/getting_started/install_library/
- Selenium Java API: https://www.selenium.dev/selenium/docs/api/java/index.html
- TestNG lifecycle, suites, listeners and concurrency: https://testng.org/
- TestNG annotations: https://testng.org/annotations.html
- REST Assured current releases and Java 17 baseline: https://rest-assured.io/
- JSONPlaceholder documented fake-write behavior: https://github.com/typicode/jsonplaceholder/blob/master/templates/GUIDE.md
- SauceDemo application source and deployment: https://github.com/saucelabs/sample-app-web
- Extent Reports sunset notice: https://github.com/extent-framework/extentreports-java
- GitHub-hosted Ubuntu browser/tool inventory: https://github.com/actions/runner-images/blob/main/images/ubuntu/Ubuntu2404-Readme.md

Selenium 4.50.0 and REST Assured 6.0.1 were selected from the official release information. Other dependencies are pinned explicit releases, not claimed to be the latest. The Maven repository was not reachable from the delivery shell, so resolution and transitive dependency compatibility are unverified. The live SauceDemo DOM could not be inspected here; locators must still be confirmed by running the UI suite.

Workflow action majors follow the official examples reviewed at delivery: checkout v7, setup-java v6 and upload-artifact v7.

- https://github.com/actions/checkout
- https://github.com/actions/setup-java
- https://github.com/actions/upload-artifact
