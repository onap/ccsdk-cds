---
description: 'Update parent POM version across all modules in the cds project by running updParentVersion.sh from ccsdk-parent'
name: "update-parent-version"
agent: agent
tools: [execute, vscode/memory, vscode/askQuestions]
model: Claude Sonnet 4.6
---

# Update Parent Version

## Task

Update all parent POM versions in the cds project by running the `updParentVersion.sh` script located in the `tools/` directory of the ccsdk/parent project.

## Inputs

${NEW_VERSION} <!-- The new parent version to set -->

## Steps

1. **Check memory for saved path**: Read `/memories/repo/ccsdk-parent-path.md`.
   - If it contains a valid directory path → use it and skip to step 4.
   - If the file is missing or empty → continue to step 2.
2. **Check default location**: Check whether `../parent` exists relative to the root directory of the cds project.
   - If it exists → use that path and continue to step 3.
   - If it does not exist → ask the user to provide the path to the ccsdk-parent project directory, then continue to step 3.
3. **Store the path**: Save the resolved ccsdk-parent directory path to `/memories/repo/ccsdk-parent-path.md` for subsequent calls.
4. **Verify script exists**: Confirm that `tools/updParentVersion.sh` exists inside the resolved ccsdk-parent directory.
5. **Run the script**: From this repository's root directory, execute:
   ```
   bash <ccsdk-parent-path>/tools/updParentVersion.sh . ${NEW_VERSION}
   ```
   The first argument (`.`) is the root directory of files to update; the second is the new parent version.
6. **Handle failures**: If the script exits with a non-zero status, capture the error output, display it to the user, and stop. Do not proceed to verification.
7. **Verification**: After the script runs successfully, verify that the parent versions in the POM files have been updated to the new version. Search for the old version string in the POM files and confirm it has been replaced. If any files were not updated, report which ones and suggest manual review.
8. **Report results**: Show the script output and summarize which files were modified.

## Constraints

- Never hardcode credentials or secrets.
- Do not modify the `updParentVersion.sh` script itself.
- Always confirm the ccsdk-parent path exists before running the script.
- If the stored path in memory no longer exists or is inaccessible, prompt the user again and update memory.
