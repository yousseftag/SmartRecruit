# Git Workflow & Contribution Guide

Welcome to the team! To keep our codebase clean and organized across our different folders (Frontend, Backend, NLP), we use a simple, structured Git workflow. 

Please read this quick guide before you start coding!

---

## 1. Initial Setup (Do this once!)
After you clone the repository to your computer, you **must** configure Git to use our custom commit template. Open your terminal in the root folder of this project and run:

```bash
git config --local commit.template .gitmessage
```
*Why?* This ensures that whenever you commit your code, a helpful template will pop up to format your message correctly.

---

## 2. Daily Workflow: Step-by-Step

### Step 1: Get the latest code
Always make sure you have the latest code from the develop branch before starting new work:
```bash
git checkout develop
git pull origin develop
```

### Step 2: Create a new branch
Never work directly on the `develop` branch. Create a new branch for your feature or bugfix. Name it something descriptive:
```bash
# Example: git checkout -b frontend/login-page
git checkout -b <folder>/<short-description>
```

### Step 3: Make your changes & Stage them
Work on your code. When you are ready to save your progress, "stage" the files you changed:
```bash
# To stage all changes:
git add .

# Or to stage specific files:
git add path/to/file
```

### Step 4: Commit your changes
Now, save your changes to history. Run the commit command **without** the `-m` flag:
```bash
git commit
```
Your text editor will automatically open with our team template. Simply replace the top line with your summary (e.g., `[Frontend]: Added login page`), add any bullet points if needed, and save/close the file to complete the commit.

### Step 5: Push your branch
Push your newly created branch up to the remote repository (GitHub/GitLab/Bitbucket):
```bash
git push -u origin <your-branch-name>
```

### Step 6: Create a Pull Request (PR)
1. Go to the repository in your web browser.
2. You will see a green prompt to "Compare & pull request" for your recently pushed branch. Click it!
3. Add a brief description of what you did and ask a team member to review it.
4. Once reviewed and approved, your code will be merged into `develop`!
