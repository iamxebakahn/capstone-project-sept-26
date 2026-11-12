---
name: review-checklist
description: Our team checklist for reviewing a change in the catalog service. Use it for any code review request.
---

- Controller only maps HTTP to a service call. No business logic.
- DTOs go in and out of controllers. Never return an entity.
- Every new rule has a unit test that fails without the code.
- A missing product answers 404, not 500 and not null.
- No new dependency in pom.xml unless it was asked for.