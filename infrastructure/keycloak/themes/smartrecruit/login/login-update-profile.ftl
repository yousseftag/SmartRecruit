<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Update Profile - Smart Recruit</title>
    <link rel="stylesheet" href="${url.resourcesPath}/css/login.css">
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap" rel="stylesheet">
</head>
<body>
<div class="login-card">

    <div class="brand-header">
        <img src="${url.resourcesPath}/img/logo.png" alt="Logo" class="logo-img" onerror="this.style.display='none'">
        <div class="brand-name">Smart Recruit</div>
    </div>

    <h1 class="page-title">Update Profile</h1>

    <#if message?has_content && message.type == 'error'>
        <div class="alert alert-error" style="margin-bottom: 15px; padding: 10px; border-radius: 6px; background-color: #fee2e2; color: #991b1b; font-size: 13px;">
            ${message.summary}
        </div>
    </#if>

    <form action="${url.loginAction}" method="post">

        <div class="field">
            <label for="email" class="label">Email</label>
            <input id="email" name="email" type="email" class="input" value="${(user.email!'')}" required autofocus>
        </div>

        <div class="field">
            <label for="firstName" class="label">First Name</label>
            <input id="firstName" name="firstName" type="text" class="input" value="${(user.firstName!'')}" required>
        </div>

        <div class="field">
            <label for="lastName" class="label">Last Name</label>
            <input id="lastName" name="lastName" type="text" class="input" value="${(user.lastName!'')}" required>
        </div>

        <div style="display: flex; gap: 12px; margin-top: 20px;">
            <#if isAppInitiatedAction?? && isAppInitiatedAction>
                <button type="submit" name="cancel-aia" value="true" class="btn" style="flex: 1; background-color: #f1f5f9; color: #475569; border: 1px solid #cbd5e1;" formnovalidate>Cancel</button>
            </#if>
            <button type="submit" class="btn btn-primary" style="flex: 2;">Save Profile</button>
        </div>
    </form>
</div>
</body>
</html>
