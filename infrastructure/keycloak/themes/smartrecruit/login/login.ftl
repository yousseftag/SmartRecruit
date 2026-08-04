<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Connexion à Smart Recruit</title>
    <link rel="stylesheet" href="${url.resourcesPath}/css/login.css">
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap" rel="stylesheet">
</head>
<body>
<div class="login-card">

    <div class="brand-header">
        <img src="${url.resourcesPath}/img/logo.png" alt="Logo" class="logo-img" onerror="this.style.display='none'">
        <div class="brand-name">Smart Recruit</div>
    </div>

    <h1 class="page-title">Connexion</h1>

    <#if message?has_content && message.type == 'error'>
        <div class="alert alert-error" style="margin-bottom: 15px; padding: 10px; border-radius: 6px; background-color: #fee2e2; color: #991b1b; font-size: 13px;">
            ${message.summary}
        </div>
    </#if>

    <form action="${url.loginAction}" method="post">

        <div class="field">
            <label for="username" class="label">Nom d'utilisateur</label>
            <input id="username" name="username" type="text" class="input" value="${(login.username!'')}" autofocus required>
        </div>

        <div class="field">
            <label for="password" class="label">Mot de passe</label>
            <input id="password" name="password" type="password" class="input" required>
        </div>

        <div class="row-between">
            <label class="checkbox-label">
                <input type="checkbox" name="rememberMe" id="rememberMe" class="custom-checkbox" <#if login.rememberMe?? && login.rememberMe == "on">checked</#if>>
                Se souvenir de moi
            </label>

            <a href="${url.loginResetCredentialsUrl}" class="forgot-link">Mot de passe oublié ?</a>
        </div>

        <button type="submit" class="btn btn-primary btn-block">Connexion</button>
    </form>

</div>
</body>
</html>