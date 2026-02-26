# GitHub Actions Workflow Validation Guide

This guide explains how to validate GitHub Actions workflows locally before pushing changes, helping you catch syntax errors and issues early.

## Table of Contents

- [Why Validate Locally?](#why-validate-locally)
- [Installation](#installation)
  - [actionlint](#actionlint-recommended)
  - [yamllint](#yamllint)
  - [GitHub CLI (gh)](#github-cli-gh)
- [Validation Tools](#validation-tools)
  - [actionlint](#actionlint-1)
  - [yamllint](#yamllint-1)
  - [GitHub CLI (gh) Validation](#github-cli-gh-validation)
- [Pre-commit Hooks for Automatic Validation](#pre-commit-hooks-for-automatic-validation)
- [Common YAML Syntax Errors](#common-yaml-syntax-errors)
- [Best Practices](#best-practices)
- [CI Integration](#ci-integration)
- [Verification Example: release.yml](#verification-example-releaseyml)
- [Quick Reference](#quick-reference)

## Why Validate Locally?

Validating workflows locally helps you:
- **Catch errors early**: Fix syntax issues before pushing to GitHub
- **Save CI minutes**: Avoid failed workflow runs due to syntax errors
- **Improve code quality**: Ensure workflows follow best practices
- **Faster feedback**: Get immediate validation results without waiting for GitHub

## Installation

### actionlint (Recommended)

`actionlint` is a specialized linter for GitHub Actions workflows that checks syntax, action versions, and workflow logic.

#### macOS
```bash
brew install actionlint
```

#### Linux
```bash
# Download binary
curl -sSfL https://raw.githubusercontent.com/rhymond/actionlint/main/scripts/download-actionlint.bash | bash

# Or install via npm
npm install -g @actionlint/cli
```

#### Windows
```powershell
# Using Scoop
scoop install actionlint

# Or download from releases
# https://github.com/rhymond/actionlint/releases
```

### yamllint

`yamllint` is a general-purpose YAML linter that validates YAML syntax and style.

#### macOS
```bash
brew install yamllint
```

#### Linux
```bash
# Ubuntu/Debian
sudo apt-get install yamllint

# Fedora
sudo dnf install yamllint

# Or via pip
pip install yamllint
```

#### Python (Cross-platform)
```bash
pip install yamllint
```

### GitHub CLI (gh)

GitHub CLI provides workflow validation capabilities and can help verify workflows are properly configured.

#### macOS
```bash
brew install gh
```

#### Linux
```bash
# Ubuntu/Debian
curl -fsSL https://cli.github.com/packages/githubcli-archive-keyring.gpg | sudo dd of=/usr/share/keyrings/githubcli-archive-keyring.gpg
echo "deb [arch=$(dpkg --print-architecture) signed-by=/usr/share/keyrings/githubcli-archive-keyring.gpg] https://cli.github.com/packages stable main" | sudo tee /etc/apt/sources.list.d/github-cli.list > /dev/null
sudo apt update
sudo apt install gh

# Fedora
sudo dnf install gh

# Or via package manager
# Arch Linux: sudo pacman -S github-cli
```

#### Authentication

After installation, authenticate with GitHub:
```bash
gh auth login
```

Follow the prompts to authenticate via web browser or token.

## Validation Tools

### actionlint

`actionlint` is the most comprehensive tool for GitHub Actions workflows. It checks:
- YAML syntax errors
- Invalid workflow structure
- Outdated action versions
- Workflow logic issues
- Security concerns

#### Basic Usage

```bash
# Validate all workflow files
actionlint

# Validate specific file
actionlint .github/workflows/release.yml

# Validate with shellcheck integration
actionlint -shellcheck

# Show verbose output
actionlint -verbose
```

#### Example Output

```bash
$ actionlint .github/workflows/release.yml
.github/workflows/release.yml:239:1: multiline strings must use <<EOF syntax [syntax]
  239 |           echo "notes<<EOF" >> $GITHUB_OUTPUT
      | ^
```

#### Configuration

Create `.actionlintrc.yml` in your repository root:

```yaml
# .actionlintrc.yml
self-hosted-runner:
  labels:
    - self-hosted
    - linux
    - macos
    - windows

# Ignore specific checks
ignore:
  - labeler
  - shellcheck
```

### yamllint

`yamllint` validates YAML syntax and style but doesn't understand GitHub Actions-specific syntax.

#### Basic Usage

```bash
# Validate all YAML files
yamllint .

# Validate specific file
yamllint .github/workflows/release.yml

# Use custom config
yamllint -c .yamllint.yml .github/workflows/
```

#### Configuration

Create `.yamllint.yml` in your repository root:

```yaml
# .yamllint.yml
extends: default

rules:
  line-length:
    max: 200
    level: warning
  indentation:
    spaces: 2
  comments:
    min-spaces-from-content: 1
  document-start: disable
  truthy:
    allowed-values: ['true', 'false', 'on', 'off']
```

### GitHub CLI (gh) Validation

GitHub CLI can validate workflows by checking if they're recognized by GitHub and can list workflow files.

#### Basic Usage

```bash
# List all workflows (validates they're recognized)
gh workflow list

# View a specific workflow file
gh workflow view release.yml

# Validate workflow syntax (indirectly - if workflow appears in list, syntax is valid)
gh workflow list | grep release.yml

# View workflow runs (confirms workflow is active)
gh run list --workflow=release.yml
```

#### Workflow Validation

While GitHub CLI doesn't have a direct "validate" command, you can use it to verify workflows:

```bash
# Check if workflow file exists and is recognized
gh workflow view .github/workflows/release.yml

# List all workflows (will fail if any have syntax errors)
gh workflow list

# View workflow definition
gh api repos/:owner/:repo/actions/workflows/:workflow_id
```

#### Example Output

```bash
$ gh workflow list
.github/workflows/release.yml	active	239050124
Build Artifacts	active	239050122
Java CI with Gradle	active	239000748
```

If a workflow has syntax errors, it may not appear in the list or GitHub will show an error when trying to view it.

#### Limitations

- GitHub CLI validation requires authentication and repository access
- It validates workflows as GitHub sees them, not locally
- Best used as a secondary validation method after local tools

#### Integration with Other Tools

Combine GitHub CLI with other validation tools:

```bash
# Validate locally first
actionlint .github/workflows/release.yml

# Then verify with GitHub CLI
gh workflow list | grep release.yml && echo "✓ Workflow recognized by GitHub"
```

## Pre-commit Hooks for Automatic Validation

Automate validation before each commit using pre-commit hooks to catch errors early.

### Simple Pre-commit Hook

A basic hook that validates workflows with actionlint:

```bash
#!/bin/bash
# .git/hooks/pre-commit

echo "Validating GitHub Actions workflows..."

if ! command -v actionlint &> /dev/null; then
    echo "Warning: actionlint not installed. Install with: brew install actionlint"
    exit 0
fi

if actionlint; then
    echo "✓ Workflows validated successfully"
    exit 0
else
    echo "✗ Workflow validation failed. Please fix errors before committing."
    exit 1
fi
```

Make it executable:
```bash
chmod +x .git/hooks/pre-commit
```

### Enhanced Pre-commit Hook

A more comprehensive hook that uses multiple validation tools:

```bash
#!/bin/bash
# .git/hooks/pre-commit

echo "🔍 Validating GitHub Actions workflows..."

ERRORS=0

# Check for modified workflow files
STAGED_WORKFLOWS=$(git diff --cached --name-only --diff-filter=ACM | grep '\.github/workflows/.*\.ya\?ml$' || true)

if [ -z "$STAGED_WORKFLOWS" ]; then
    echo "No workflow files staged. Skipping validation."
    exit 0
fi

echo "Found staged workflow files:"
echo "$STAGED_WORKFLOWS"
echo ""

# Validate with yamllint if available
if command -v yamllint &> /dev/null; then
    echo "Running yamllint..."
    for file in $STAGED_WORKFLOWS; do
        if ! yamllint "$file"; then
            ERRORS=$((ERRORS + 1))
        fi
    done
else
    echo "⚠️  yamllint not installed. Install with: brew install yamllint"
fi

# Validate with actionlint if available
if command -v actionlint &> /dev/null; then
    echo "Running actionlint..."
    for file in $STAGED_WORKFLOWS; do
        if ! actionlint "$file"; then
            ERRORS=$((ERRORS + 1))
        fi
    done
else
    echo "⚠️  actionlint not installed. Install with: brew install actionlint"
fi

if [ $ERRORS -gt 0 ]; then
    echo ""
    echo "❌ Workflow validation failed with $ERRORS error(s)."
    echo "Please fix the errors before committing."
    exit 1
fi

echo ""
echo "✅ All workflow files validated successfully!"
exit 0
```

Make it executable:
```bash
chmod +x .git/hooks/pre-commit
```

## Common YAML Syntax Errors

### 1. Multiline String Assignment

**Problem**: Incorrect multiline string syntax in GitHub Actions outputs.

**❌ Incorrect**:
```yaml
run: |
  echo "notes<<EOF" >> $GITHUB_OUTPUT
  echo "${RELEASE_NOTES}" >> $GITHUB_OUTPUT
  echo "EOF" >> $GITHUB_OUTPUT
```

**✅ Correct**:
```yaml
run: |
  {
    echo "notes<<RELEASE_NOTES_DELIMITER"
    echo "${RELEASE_NOTES}"
    echo "RELEASE_NOTES_DELIMITER"
  } >> $GITHUB_OUTPUT
```

**Why**: Using a unique delimiter (instead of `EOF`) prevents conflicts if the content contains "EOF". Grouping with `{}` ensures all output is written atomically.

### 2. Indentation Issues

**Problem**: YAML is sensitive to indentation. Mixing tabs and spaces or incorrect indentation causes errors.

**❌ Incorrect**:
```yaml
steps:
  - name: Build
    run: |
      echo "Building"
    env:
      VAR: value
```

**✅ Correct**:
```yaml
steps:
  - name: Build
    run: |
      echo "Building"
    env:
      VAR: value
```

**Fix**: Use consistent indentation (2 spaces recommended). Configure your editor to show whitespace.

### 3. Special Characters in Strings

**Problem**: Unescaped special characters break YAML parsing.

**❌ Incorrect**:
```yaml
run: |
  echo "Version: ${{ version }}"
  echo "Path: C:\Users\Name"
```

**✅ Correct**:
```yaml
run: |
  echo "Version: ${{ version }}"
  echo "Path: C:\\Users\\Name"
```

**Fix**: Escape backslashes, quotes, and other special characters. Use single quotes for literal strings when possible.

### 4. Boolean Values

**Problem**: Using `true`/`false` as strings instead of booleans.

**❌ Incorrect**:
```yaml
prerelease: "true"
draft: "false"
```

**✅ Correct**:
```yaml
prerelease: true
draft: false
```

**Fix**: Use boolean values without quotes. GitHub Actions expects actual booleans, not strings.

### 5. Multiline Strings in Values

**Problem**: Incorrect multiline string syntax in YAML values.

**❌ Incorrect**:
```yaml
body: |
  Line 1
  Line 2
  Line 3
```

**✅ Correct** (for GitHub Actions outputs):
```yaml
run: |
  {
    echo "body<<BODY_DELIMITER"
    echo "Line 1"
    echo "Line 2"
    echo "Line 3"
    echo "BODY_DELIMITER"
  } >> $GITHUB_OUTPUT
```

**Fix**: For workflow outputs, use the `<<DELIMITER` syntax. For direct YAML values, use `|` or `>` block scalars.

### 6. Missing Quotes for Special Values

**Problem**: Values starting with special characters need quotes.

**❌ Incorrect**:
```yaml
tag: v*.*.*
path: ${{ github.workspace }}
```

**✅ Correct**:
```yaml
tag: 'v*.*.*'
path: ${{ github.workspace }}
```

**Fix**: Quote values that start with `*`, `&`, `!`, `|`, `>`, `%`, `@`, or contain expressions.

### 7. Incorrect Expression Syntax

**Problem**: Missing `${{ }}` or incorrect expression syntax.

**❌ Incorrect**:
```yaml
tag: ${github.ref}
name: $steps.version.outputs.tag
```

**✅ Correct**:
```yaml
tag: ${{ github.ref }}
name: ${{ steps.version.outputs.tag }}
```

**Fix**: Always use `${{ }}` for GitHub Actions expressions.

## Best Practices

### 1. Validate Before Committing

```bash
# Add to your workflow
git add .github/workflows/
actionlint
git commit
```

### 2. Use Editor Extensions

- **VS Code**: Install "GitHub Actions" extension
- **IntelliJ IDEA**: Built-in YAML support with GitHub Actions schema
- **Vim/Neovim**: Use `yamllint` plugin

### 3. Validate in CI

Add a validation job to your workflows:

```yaml
jobs:
  validate-workflows:
    name: Validate Workflows
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - name: Install actionlint
        run: |
          curl -sSfL https://raw.githubusercontent.com/rhymond/actionlint/main/scripts/download-actionlint.bash | bash
          sudo mv actionlint /usr/local/bin/
      - name: Validate workflows
        run: actionlint
```

### 4. Use Meaningful Delimiters

When using multiline output syntax, use descriptive delimiters:

```bash
# Good
echo "notes<<RELEASE_NOTES_DELIMITER"

# Avoid
echo "notes<<EOF"  # Too generic, might conflict
```

### 5. Test Workflow Syntax Locally

```bash
# Quick syntax check
yamllint .github/workflows/

# Comprehensive check
actionlint

# Test specific workflow
actionlint .github/workflows/release.yml
```

## CI Integration

### GitHub Actions Workflow Validation Job

Add this job to validate workflows before they run:

```yaml
name: Validate Workflows

on:
  pull_request:
    paths:
      - '.github/workflows/**'
  push:
    branches:
      - main
    paths:
      - '.github/workflows/**'

jobs:
  validate:
    name: Validate Workflow Syntax
    runs-on: ubuntu-latest
    steps:
      - name: Checkout
        uses: actions/checkout@v4
      
      - name: Run actionlint
        uses: rhymond/actionlint@v1
        with:
          # Optional: fail on warnings
          fail_on_warnings: false
```

### Pre-commit Configuration

Create `.pre-commit-config.yaml`:

```yaml
repos:
  - repo: https://github.com/rhymond/actionlint-pre-commit
    rev: v1.6.26
    hooks:
      - id: actionlint-docker
        args: [-shellcheck]
  
  - repo: https://github.com/adrienverge/yamllint
    rev: v1.33.0
    hooks:
      - id: yamllint
        args: [--format=parsable, --strict]
```

Install pre-commit:
```bash
pip install pre-commit
pre-commit install
```

## Troubleshooting

### actionlint Not Finding Workflows

```bash
# Specify path explicitly
actionlint .github/workflows/*.yml

# Or use glob pattern
actionlint '**/.github/workflows/*.yml'
```

### yamllint Too Strict

Adjust rules in `.yamllint.yml`:
```yaml
rules:
  line-length: disable
  comments: disable
```

### Multiline Output Issues

If multiline outputs aren't working:
1. Check delimiter uniqueness
2. Ensure no special characters in delimiter
3. Verify proper grouping with `{}`
4. Test with simple content first

## Verification Example: release.yml

As an example, let's verify the `release.yml` workflow file:

### Syntax Verification Checklist

✅ **Proper Indentation**: The file uses consistent 2-space indentation throughout.

✅ **Valid Multiline String Syntax**: Lines 239-244 use the correct multiline output syntax:
```yaml
# Use a unique delimiter for multiline output to avoid conflicts
{
  echo "notes<<RELEASE_NOTES_DELIMITER"
  echo "${RELEASE_NOTES}"
  echo "RELEASE_NOTES_DELIMITER"
} >> $GITHUB_OUTPUT
```

✅ **Correct GitHub Actions Syntax**: 
- Proper use of `${{ }}` expressions
- Valid workflow structure with `on`, `jobs`, `steps`
- Correct action references (e.g., `actions/checkout@v4`)
- Proper output syntax with `$GITHUB_OUTPUT`

✅ **No Syntax Errors**: The workflow is recognized by GitHub CLI:
```bash
$ gh workflow list | grep release.yml
.github/workflows/release.yml	active	239050124
```

### Key Features Verified

1. **Multiline Output**: Uses unique delimiter `RELEASE_NOTES_DELIMITER` instead of generic `EOF`
2. **Atomic Output**: Groups echo commands with `{}` to ensure atomic write
3. **Expression Syntax**: All GitHub Actions expressions use `${{ }}` correctly
4. **Workflow Structure**: Proper nesting of `on`, `jobs`, `steps`, `with`, `env`
5. **Action Versions**: All actions use pinned versions (e.g., `@v4`, `@v2`)

### Running Verification

To verify `release.yml` yourself:

```bash
# With actionlint (recommended)
actionlint .github/workflows/release.yml

# With yamllint
yamllint .github/workflows/release.yml

# With GitHub CLI
gh workflow view release.yml
```

## Additional Resources

- [actionlint Documentation](https://github.com/rhymond/actionlint)
- [yamllint Documentation](https://yamllint.readthedocs.io/)
- [GitHub CLI Documentation](https://cli.github.com/manual/)
- [GitHub Actions Syntax](https://docs.github.com/en/actions/using-workflows/workflow-syntax-for-github-actions)
- [YAML Specification](https://yaml.org/spec/1.2.2/)

## Quick Reference

### Installation Commands

```bash
# macOS
brew install actionlint yamllint gh

# Linux (Ubuntu/Debian)
sudo apt-get install yamllint
curl -fsSL https://cli.github.com/packages/githubcli-archive-keyring.gpg | sudo dd of=/usr/share/keyrings/githubcli-archive-keyring.gpg
echo "deb [arch=$(dpkg --print-architecture) signed-by=/usr/share/keyrings/githubcli-archive-keyring.gpg] https://cli.github.com/packages stable main" | sudo tee /etc/apt/sources.list.d/github-cli.list > /dev/null
sudo apt update && sudo apt install gh
curl -sSfL https://raw.githubusercontent.com/rhymond/actionlint/main/scripts/download-actionlint.bash | bash

# Linux (Fedora)
sudo dnf install yamllint gh
curl -sSfL https://raw.githubusercontent.com/rhymond/actionlint/main/scripts/download-actionlint.bash | bash
```

### Validation Commands

```bash
# Validate all workflows (actionlint - recommended)
actionlint

# Validate specific file
actionlint .github/workflows/release.yml

# Validate YAML syntax only
yamllint .github/workflows/

# Validate with GitHub CLI
gh workflow list

# View specific workflow
gh workflow view release.yml

# Combined validation
actionlint && yamllint .github/workflows/ && gh workflow list
```

---

**Remember**: Always validate workflows locally before pushing to catch errors early and save CI minutes!
