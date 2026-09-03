<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Mot de passe oublié - Smart Recruit</title>
    <link rel="stylesheet" href="${url.resourcesPath}/css/login.css">
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap" rel="stylesheet">
</head>
<body>
<div class="login-card">

    <div class="brand-header">
        <img src="${url.resourcesPath}/img/logo.png" alt="Logo" class="logo-img" onerror="this.style.display='none'">
        <div class="brand-name">Smart Recruit</div>
    </div>

    <h1 class="page-title">Mot de passe oublié ?</h1>
    <p class="subtitle" style="margin-bottom: 20px; color: #64748b; font-size: 14px; text-align: center;">
        Entrez votre adresse email et nous vous enverrons les instructions pour réinitialiser votre mot de passe.
    </p>

    <#if message?has_content>
        <div class="alert alert-${message.type}" style="margin-bottom: 15px; padding: 10px; border-radius: 6px;
        <#if message.type == 'error'>background-color: #fee2e2; color: #991b1b;<#else>background-color: #dcfce3; color: #166534;</#if> font-size: 13px;">
            ${message.summary}
        </div>
    </#if>

    <form action="${url.loginAction}" method="post">

        <div class="field">
            <label for="username" class="label">Email ou Nom d'utilisateur</label>
            <input id="username" name="username" type="text" class="input" value="${(auth.attemptedUsername!'')}" autofocus required>
        </div>

        <div style="display: flex; flex-direction: column; align-items: center; gap: 18px; margin-top: 24px;">
            <button type="submit" class="btn btn-primary">Envoyer les instructions</button>
            <a href="${url.loginUrl}" class="btn" style="background-color: #f1f5f9; color: #475569; border: 1px solid #cbd5e1; text-decoration: none;">Retour à la connexion</a>
        </div>

    </form>
</div>
</body>
</html>