---
name: 🐛 Bug Report
about: Create a report to help us improve.
title: 'fix: '
labels: 'bug'
assignees: ''
---

## 🚨 Problem Description
When the user tries to [action], the system [wrong behavior] instead of [expected behavior].

## 🧪 Steps to Reproduce
1. Go to '[Page Name]'
2. Click on '[Button Name]'
3. See error [e.g., 500 Internal Server Error]

## 💡 Proposed Solution / Root Cause
- **Cause**: Environment variable `BASE_URL` is missing in the production config.
- **Fix**: Update the Dockerfile/Config to include the correct env path.

## 📸 Screenshots (If applicable)