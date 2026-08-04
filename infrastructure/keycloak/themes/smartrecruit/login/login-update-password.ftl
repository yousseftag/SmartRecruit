<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Update Password - Smart Recruit</title>
    <link rel="stylesheet" href="${url.resourcesPath}/css/login.css">
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap" rel="stylesheet">
</head>
<body>
<div class="login-card">

    <div class="brand-header">
        <img src="${url.resourcesPath}/img/logo.png" alt="Logo" class="logo-img" onerror="this.style.display='none'">
        <div class="brand-name">Smart Recruit</div>
    </div>

    <h1 class="page-title">Change Password</h1>
    <p class="subtitle" style="margin-bottom: 20px; color: #94a3b8; font-size: 14px;">
        Please enter your new password to secure your account.
    </p>

    <#if message?has_content && message.type == 'error'>
        <div class="alert alert-error" style="margin-bottom: 15px; padding: 10px; border-radius: 6px; background-color: #fee2e2; color: #991b1b; font-size: 13px;">
            ${message.summary}
        </div>
    </#if>

    <form action="${url.loginAction}" method="post">

        <div class="field">
            <label for="password-new" class="label">New Password</label>
            <input id="password-new" name="password-new" type="password" class="input" required autofocus autocomplete="new-password">
        </div>

        <div class="field">
            <label for="password-confirm" class="label">Confirm New Password</label>
            <input id="password-confirm" name="password-confirm" type="password" class="input" required autocomplete="new-password">
        </div>

        <div style="display: flex; gap: 12px; margin-top: 20px;">
            <#if isAppInitiatedAction?? && isAppInitiatedAction>
                <button type="submit" name="cancel-aia" value="true" class="btn" style="flex: 1; background-color: #f1f5f9; color: #475569; border: 1px solid #cbd5e1;" formnovalidate>Cancel</button>
            </#if>
            <button type="submit" class="btn btn-primary" style="flex: 2;">Update Password</button>
        </div>
    </form>
</div>
</body>
</html>