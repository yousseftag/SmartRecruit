<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Mettre à jour le mot de passe - Smart Recruit</title>
    <link rel="stylesheet" href="${url.resourcesPath}/css/login.css">
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap" rel="stylesheet">
</head>
<body>
<div class="login-card">

    <div class="brand-header">
        <img src="${url.resourcesPath}/img/logo.png" alt="Logo" class="logo-img" onerror="this.style.display='none'">
        <div class="brand-name">Smart Recruit</div>
    </div>

    <h1 class="page-title">Changer le mot de passe</h1>
    <p class="subtitle" style="margin-bottom: 20px; color: #94a3b8; font-size: 14px;">
        Veuillez entrer votre nouveau mot de passe pour sécuriser votre compte.
    </p>

    <#if message?has_content && message.type == 'error'>
        <div class="alert alert-error" style="margin-bottom: 15px; padding: 10px; border-radius: 6px; background-color: #fee2e2; color: #991b1b; font-size: 13px;">
            ${message.summary}
        </div>
    </#if>

    <form action="${url.loginAction}" method="post">

        <div class="field">
            <label for="password-new" class="label">Nouveau mot de passe</label>
            <input id="password-new" name="password-new" type="password" class="input" required autofocus autocomplete="new-password">
        </div>

        <div class="field">
            <label for="password-confirm" class="label">Confirmer le nouveau mot de passe</label>
            <input id="password-confirm" name="password-confirm" type="password" class="input" required autocomplete="new-password">
        </div>

        <div style="display: flex; gap: 12px; margin-top: 20px;">
            <#if isAppInitiatedAction?? && isAppInitiatedAction>
                <button type="submit" name="cancel-aia" value="true" class="btn" style="flex: 1; background-color: #f1f5f9; color: #475569; border: 1px solid #cbd5e1;" formnovalidate>Annuler</button>
            </#if>
            <button type="submit" class="btn btn-primary" style="flex: 2;">Mettre à jour le mot de passe</button>
        </div>
    </form>
</div>
</body>
</html>