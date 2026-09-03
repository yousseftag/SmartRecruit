-- V6__email_templates.sql
-- Creates email_template table for customizable recruitment email templates

CREATE TABLE email_template (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    template_key VARCHAR(100) NOT NULL DEFAULT 'OTHER',
    name VARCHAR(255) NOT NULL,
    subject VARCHAR(500) NOT NULL,
    body_html TEXT NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_email_template_key ON email_template(template_key);

CREATE TRIGGER trg_email_template_updated_at
    BEFORE UPDATE ON email_template
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

-- Seed standard recruitment workflow templates
INSERT INTO email_template (template_key, name, subject, body_html) VALUES
(
  'INTERVIEW_INVITATION',
  'Convocation à un entretien',
  'Convocation à un entretien — {{titre_offre}}',
  '<p>Bonjour {{nom_candidat}},</p>

<p>Nous avons le plaisir de vous convier à un entretien pour le poste de <strong>{{titre_offre}}</strong>.</p>

<p>Merci de nous confirmer votre disponibilité en répondant à cet email.</p>

<p>Cordialement,<br>
{{nom_recruteur}}<br>
SmartRecruit</p>'
),
(
  'FOLLOW_UP',
  'Relance candidat',
  'Relance concernant votre candidature — {{titre_offre}}',
  '<p>Bonjour {{nom_candidat}},</p>

<p>Nous revenons vers vous concernant votre candidature au poste de <strong>{{titre_offre}}</strong>.</p>

<p>Nous souhaiterions savoir si vous êtes toujours disponible et intéressé(e) par cette opportunité.</p>

<p>Cordialement,<br>
{{nom_recruteur}}<br>
SmartRecruit</p>'
),
(
  'REJECTION',
  'Refus de candidature',
  'Réponse concernant votre candidature — {{titre_offre}}',
  '<p>Bonjour {{nom_candidat}},</p>

<p>Nous vous remercions de l''intérêt porté à notre entreprise et du temps consacré à votre candidature pour le poste de <strong>{{titre_offre}}</strong>.</p>

<p>Après étude attentive de votre profil, nous avons le regret de vous informer que nous ne donnons pas suite à votre candidature pour ce poste.</p>

<p>Nous conservons votre profil et ne manquerons pas de vous recontacter si une opportunité correspondant à vos compétences se présente.</p>

<p>Cordialement,<br>
{{nom_recruteur}}<br>
SmartRecruit</p>'
);


