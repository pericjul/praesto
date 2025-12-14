![Workflow](https://github.com/pericjul/praesto/actions/workflows/ci-badge.yml/badge.svg)
![Coverage](https://github.com/pericjul/praesto/blob/main/.github/badges/jacoco.svg)
![Branches](https://github.com/pericjul/praesto/blob/main/.github/badges/branches.svg)

## Praesto
Praesto ist eine KI-gestützte Webplattform, die Schüler:innen im Bewerbungsprozess unterstützt.
Es werden zwei Rollen unterschieden:
- **Student**: Kann KI-Bewerbungstrainings durchführen, Bewerbungen verwalten, Notizen erstellen und Badges verdienen.
- **Teacher**: Kann Klassen verwalten, Aufgaben erstellen und Schüler-Abgaben bewerten.

erDiagram
  USER ||--o{ SUBMISSION : "macht"
  USER ||--o{ BADGE_AWARD : "erhält"
  TEACHER ||--o{ TASK : "erstellt"
  TASK ||--o{ SUBMISSION : "hat"
  TASK ||--o{ QUESTION : "enthält"
  SUBMISSION ||--o{ CHATMESSAGE : "beinhaltet"
  BADGE ||--o{ BADGE_AWARD : "ist vergeben"

  USER {
    string _id
    string name
    string email
    string role  // "student" | "teacher"
    string auth0Id
  }

  TEACHER {
    string _id
    string name
    string email
  }

  TASK {
    string _id
    string title
    string classId
    string createdBy // teacherId
    string state     // draft | published | archived
    datetime dueAt
  }

  QUESTION {
    string _id
    string taskId
    string text
    int order
  }

  SUBMISSION {
    string _id
    string taskId
    string studentId
    string state     // assigned | in_progress | submitted | graded
    datetime submittedAt
    float points
    string teacherFeedback
  }

  CHATMESSAGE {
    string _id
    string submissionId
    string sender    // student | ai | teacher
    string content
    datetime createdAt
  }

  BADGE {
    string _id
    string code
    string title
    string criteria
  }

  BADGE_AWARD {
    string _id
    string badgeId
    string userId
    datetime awardedAt
  }