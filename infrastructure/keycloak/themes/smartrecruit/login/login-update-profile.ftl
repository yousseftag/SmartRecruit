<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Compléter votre profil - Smart Recruit</title>
    <link rel="stylesheet" href="${url.resourcesPath}/css/login.css">
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap" rel="stylesheet">
</head>
<body>
<div class="login-card">

    <div class="brand-header">
        <img src="${url.resourcesPath}/img/logo.png" alt="Logo" class="logo-img" onerror="this.style.display='none'">
        <div class="brand-name">Smart Recruit</div>
    </div>

    <h1 class="page-title">Compléter votre profil</h1>
    <p class="subtitle" style="margin-bottom: 24px; color: #94a3b8; font-size: 14px;">
        Veuillez vérifier et compléter vos informations personnelles pour finaliser votre inscription.
    </p>

    <#assign emailVal = (user.email!'')?has_content?then(user.email, ((profile.attributesByName.email.value)!''))>
    <#assign firstNameVal = (user.firstName!'')?has_content?then(user.firstName, ((profile.attributesByName.firstName.value)!''))>
    <#assign lastNameVal = (user.lastName!'')?has_content?then(user.lastName, ((profile.attributesByName.lastName.value)!''))>

    <form action="${url.loginAction}" method="post">

        <div class="field">
            <label for="firstName" class="label">Prénom</label>
            <input id="firstName" name="firstName" type="text" class="input" value="${firstNameVal}" placeholder="Votre prénom" autofocus>
        </div>

        <div class="field">
            <label for="lastName" class="label">Nom</label>
            <input id="lastName" name="lastName" type="text" class="input" value="${lastNameVal}" placeholder="Votre nom">
        </div>

        <div class="field">
            <label for="email" class="label">Adresse Email</label>
            <input id="email" name="email" type="email" class="input" value="${emailVal}" placeholder="votre.email@exemple.com" required>
        </div>

        <div style="display: flex; gap: 12px; margin-top: 24px;">
            <#if isAppInitiatedAction?? && isAppInitiatedAction>
                <button type="submit" name="cancel-aia" value="true" class="btn" style="flex: 1; background-color: #f1f5f9; color: #475569; border: 1px solid #cbd5e1;" formnovalidate>Annuler</button>
            </#if>
            <button type="submit" class="btn btn-primary" style="flex: 2;">Enregistrer et continuer</button>
        </div>
    </form>
</div>
</body>
</html>
