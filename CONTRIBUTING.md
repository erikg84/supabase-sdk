# Contributing to Supabase SDK

Thank you for your interest in contributing to the Supabase SDK project! This document provides guidelines and instructions for contributing.

## Code of Conduct

- Be respectful and inclusive
- Welcome newcomers and help them learn
- Focus on constructive criticism
- Keep discussions professional and on-topic

## Getting Started

### Prerequisites

- JDK 17 or later
- Git
- Basic knowledge of Kotlin and Kotlin Multiplatform
- For iOS development: macOS with Xcode
- For Android development: Android SDK

### Setting Up Your Environment

1. Fork the repository on GitHub
2. Clone your fork:
   ```bash
   git clone https://github.com/YOUR-USERNAME/supabase-sdk.git
   cd supabase-sdk
   ```
3. Add upstream remote:
   ```bash
   git remote add upstream https://github.com/erikg84/supabase-sdk.git
   ```
4. Build the project:
   ```bash
   ./gradlew build
   ```

## Development Workflow

### 1. Create a Branch

```bash
git checkout -b feature/your-feature-name
```

Use prefixes:
- `feature/` for new features
- `fix/` for bug fixes
- `docs/` for documentation
- `refactor/` for code refactoring

### 2. Make Your Changes

- Write clean, readable code
- Follow Kotlin coding conventions
- Use explicit API mode for public APIs
- Add KDoc comments for public APIs
- Keep changes focused and atomic

### 3. Test Your Changes

```bash
# Run all tests
./gradlew test

# Test specific module
./gradlew :supabase-core:test

# Build all modules
./gradlew build
```

### 4. Commit Your Changes

Write clear, descriptive commit messages:

```bash
git add .
git commit -m "feat: add email verification to auth module

- Add verifyEmail method to SupabaseAuth
- Add tests for email verification
- Update documentation"
```

Commit message format:
- `feat:` new feature
- `fix:` bug fix
- `docs:` documentation changes
- `test:` adding or updating tests
- `refactor:` code refactoring
- `chore:` maintenance tasks

### 5. Push and Create Pull Request

```bash
git push origin feature/your-feature-name
```

Then create a pull request on GitHub with:
- Clear title and description
- Reference any related issues
- Explain what changed and why
- Include screenshots for UI changes

## Code Style

### Kotlin

- Follow [Kotlin coding conventions](https://kotlinlang.org/docs/coding-conventions.html)
- Use meaningful variable and function names
- Keep functions small and focused
- Use explicit types for public APIs
- Use `explicitApi()` mode

### Documentation

- Add KDoc comments for all public APIs
- Include usage examples in documentation
- Update README.md if adding major features
- Keep documentation up to date with code changes

### Example

```kotlin
/**
 * Signs in a user with email and password.
 *
 * @param email The user's email address
 * @param password The user's password
 * @return Result containing UserInfo on success, or exception on failure
 *
 * @sample
 * ```kotlin
 * val result = auth.signInWithEmail("user@example.com", "password")
 * result.onSuccess { user ->
 *     println("Signed in: ${user.email}")
 * }
 * ```
 */
public suspend fun signInWithEmail(email: String, password: String): Result<UserInfo>
```

## Testing

### Writing Tests

- Write tests for all new features
- Test edge cases and error conditions
- Use descriptive test names
- Keep tests independent and isolated

### Test Structure

```kotlin
class SupabaseAuthTest {
    @Test
    fun `signInWithEmail should return success with valid credentials`() {
        // Arrange
        val auth = SupabaseAuth(mockClient)
        
        // Act
        val result = runBlocking {
            auth.signInWithEmail("test@example.com", "password")
        }
        
        // Assert
        assertTrue(result.isSuccess)
    }
}
```

## Pull Request Process

1. **Update Documentation**: Ensure README.md and other docs reflect your changes
2. **Add Tests**: All new code should have corresponding tests
3. **Pass CI**: Ensure all CI checks pass
4. **Request Review**: Tag maintainers for review
5. **Address Feedback**: Respond to review comments promptly
6. **Squash Commits**: Maintainers may ask you to squash commits before merging

## Project Structure

```
supabase-sdk/
├── supabase-core/       # Core client and configuration
├── supabase-db/         # Database operations
├── supabase-auth/       # Authentication
├── supabase-koin/       # Dependency injection
├── supabase-auth-ui/    # UI components
├── buildSrc/            # Build configuration
└── .github/             # CI/CD workflows
```

## Module Guidelines

### Adding a New Module

1. Create module directory: `mkdir my-module`
2. Create `build.gradle.kts`
3. Add to `settings.gradle.kts`
4. Configure source sets
5. Add dependencies
6. Document the module

### Module Dependencies

- Use `api` for dependencies that consumers need
- Use `implementation` for internal dependencies
- Keep inter-module dependencies minimal
- Document dependency choices

## Release Process

Releases are managed by project maintainers:

1. Update version in `gradle.properties`
2. Update CHANGELOG.md
3. Create Git tag
4. Create GitHub release
5. GitHub Actions publishes to GitHub Packages

## Getting Help

- **Questions**: Open a GitHub Discussion
- **Bugs**: Open a GitHub Issue
- **Features**: Open a GitHub Issue with the `enhancement` label
- **Security**: Email security concerns to maintainers privately

## Recognition

Contributors will be recognized in:
- Repository contributors list
- Release notes
- CONTRIBUTORS.md file (coming soon)

## License

By contributing to this project, you agree that your contributions will be licensed under the Apache License 2.0.

## Additional Resources

- [Kotlin Multiplatform Documentation](https://kotlinlang.org/docs/multiplatform.html)
- [Supabase Documentation](https://supabase.com/docs)
- [Supabase-kt Library](https://github.com/supabase-community/supabase-kt)
- [Koin Documentation](https://insert-koin.io/)

Thank you for contributing! 🎉
