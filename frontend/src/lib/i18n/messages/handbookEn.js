// User manual – English content (template for DE/FR/IT).
export const handbookEn = {
	de: {},
	en: {
		"hb.title": "User manual",
		"hb.subtitle": "Everything you need to know about Praesto – from setup to everyday use.",
		"hb.toc": "Contents",
		"hb.backToStart": "Back to home",

		// 1 Introduction
		"hb.intro.title": "Introduction",
		"hb.intro.p1": "Praesto is a Swiss education platform that prepares young people for their apprenticeship search. At its heart is an AI coach with which students practise realistic job interviews and receive honest feedback right away.",
		"hb.intro.p2": "Teachers manage classes, assign tasks and provide feedback. School administrators keep an overview of the entire school. This manual explains every feature step by step.",

		// 2 Getting started
		"hb.start.title": "Getting started",
		"hb.start.intro": "Praesto is rolled out at your school in four simple steps:",
		"hb.start.s1t": "School is set up",
		"hb.start.s1d": "The Praesto team creates your school and sends the school administration a personal invitation link. Using this link, the administration registers and gains access to the admin area.",
		"hb.start.s2t": "Invite teachers",
		"hb.start.s2d": "From the dashboard, the school administration creates a teacher invitation link and sends it out (e.g. by email). Teachers register on their own via the link – their school is assigned automatically.",
		"hb.start.s3t": "Classes & students",
		"hb.start.s3d": "Teachers create classes and show the class invitation link or QR code in the classroom. Students join with a single click and set up their account.",
		"hb.start.s4t": "Practise & support",
		"hb.start.s4d": "Students practise job interviews with the AI and complete tasks. Teachers give feedback and track the class's progress.",

		// 3 For school administrators
		"hb.admin.title": "For school administrators",
		"hb.admin.intro": "As a school administrator you manage the entire school. After logging in you land on the admin dashboard with the following options:",
		"hb.admin.l1t": "Invite teachers",
		"hb.admin.l1d": "Create a teacher invitation link in the dashboard. The link is valid for 30 days and can be used by any number of teachers. Copy it with a single click and send it out.",
		"hb.admin.l2t": "Manage users",
		"hb.admin.l2d": "Under \"Users\" you can see all teachers and students at your school. You can filter by name, role or email and deactivate individual accounts if needed.",
		"hb.admin.l3t": "Key figures at a glance",
		"hb.admin.l3d": "The dashboard shows the number of teachers, students, classes and tasks at your school at a glance.",
		"hb.admin.l4t": "Demo access",
		"hb.admin.l4d": "With the live demo you can try Praesto in read-only mode and without obligation before you decide.",
		"hb.admin.tip": "Tip: Create just one teacher link and share it with your entire staff – everyone can use the same link.",

		// 4 For teachers
		"hb.teacher.title": "For teachers",
		"hb.teacher.intro": "As a teacher you manage your classes, assign tasks and grade submissions.",
		"hb.teacher.classesH": "Create classes",
		"hb.teacher.classesP": "Under \"Classes\" create a new class (e.g. \"3a\"). Each class belongs to your school and only you, as the teacher who created it, can manage it.",
		"hb.teacher.studentsH": "Add students",
		"hb.teacher.studentsP": "There are two ways: (1) Create a class invitation link (valid for 14 days) and show it as a QR code in the classroom – students join on their own. (2) Search for students already registered at your school using the search field and add them directly.",
		"hb.teacher.asgH": "Create tasks",
		"hb.teacher.asgP": "Create tasks with a title, description and deadline. A task can be assigned to one or several classes at once. There are five task types:",
		"hb.teacher.typeInterviewT": "🤖 AI interview",
		"hb.teacher.typeInterviewD": "Students conduct a job interview with the AI and submit it as their assignment.",
		"hb.teacher.typeDocT": "📄 Document",
		"hb.teacher.typeDocD": "Students upload a file (e.g. a CV as a PDF or image).",
		"hb.teacher.typeReflectionT": "✍️ Reflection",
		"hb.teacher.typeReflectionD": "Students write a text, e.g. a self-reflection or a cover letter.",
		"hb.teacher.typeVideoT": "🎥 Video pitch",
		"hb.teacher.typeVideoD": "Students upload a short application video.",
		"hb.teacher.typeResearchT": "🔍 Research",
		"hb.teacher.typeResearchD": "Students document their research findings with text and links.",
		"hb.teacher.fbH": "Grade & give feedback",
		"hb.teacher.fbP": "Open a task to see all submissions. Give feedback and optionally a grade. With text snippets you can write common feedback even faster. You can also see who has not submitted yet.",
		"hb.teacher.exportH": "Export submissions",
		"hb.teacher.exportP": "With a single click you can export all submissions and grades for a task as a CSV file – ideal for further processing in Excel.",

		// 5 For students
		"hb.student.title": "For students",
		"hb.student.intro": "As a student you practise job interviews, complete tasks and keep track of your applications.",
		"hb.student.trainH": "AI training",
		"hb.student.trainP": "Under \"Training\" you start a conversation with the AI coach. It asks you real interview questions and gives feedback after every answer. You can start a new session at any time or continue an old one.",
		"hb.student.trainTip": "Tip: Answer in full sentences, take your time to think and be honest – that's how you learn the most.",
		"hb.student.taskH": "Tasks",
		"hb.student.taskP": "Under \"Tasks\" you can see what your teacher has assigned you. Depending on the type, you submit an interview, a text, a document or a video. After grading, the feedback appears right next to the task.",
		"hb.student.noteH": "Notes",
		"hb.student.noteP": "Keep important information about companies and positions in notes – e.g. contact persons, impressions or open questions.",
		"hb.student.appH": "Applications",
		"hb.student.appP": "In the application tracker you organise all your applications with their status (planned, applied, invited …) and interview dates. That way you never lose track.",
		"hb.student.badgeH": "Badges & streaks",
		"hb.student.badgeP": "You collect badges for your activities – training sessions, submissions, applications. Anyone who practises several days in a row builds up a streak. That keeps you motivated.",

		// 6 Roles & permissions
		"hb.roles.title": "Roles & permissions",
		"hb.roles.intro": "Praesto has five roles with different permissions:",
		"hb.roles.superT": "Super admin",
		"hb.roles.superD": "Operator of the platform (the Praesto team). Creates schools and invites school administrators.",
		"hb.roles.adminT": "School administrator",
		"hb.roles.adminD": "Manages their own school: invites teachers, manages users, sees key figures.",
		"hb.roles.teacherT": "Teacher",
		"hb.roles.teacherD": "Manages classes, assigns tasks, grades submissions and sees progress.",
		"hb.roles.studentT": "Student",
		"hb.roles.studentD": "Practises with the AI, completes tasks, manages applications and notes, collects badges.",
		"hb.roles.demoT": "Demo access",
		"hb.roles.demoD": "Read-only test access for school administrators – can view everything but change nothing.",

		// 7 Privacy & security
		"hb.privacy.title": "Privacy & security",
		"hb.privacy.p1": "Praesto uses its own login system and no US provider. Each school is completely separated from the others – your students' data stays within your school.",
		"hb.privacy.p2": "Precisely because this involves data of minors, this separation and the hosting region are important.",
		"hb.privacy.secH": "How secure are the passwords?",
		"hb.privacy.secP": "Passwords are never stored in plain text but are encrypted with BCrypt. After logging in, a signed token (JWT) protects the session. Too many failed login attempts result in a temporary lockout.",

		// 8 FAQ
		"hb.faq.title": "Frequently asked questions",
		"hb.faq.q1": "Do students need to have an email address?",
		"hb.faq.a1": "To register via the class link, a first name, last name, email and a password are enough. A prior account is not required.",
		"hb.faq.q2": "What happens when an invitation link expires?",
		"hb.faq.a2": "Teacher links are valid for 30 days, class links for 14 days. After that you simply create a new link – old links can also be deactivated manually.",
		"hb.faq.q3": "Can a task be assigned to several classes?",
		"hb.faq.a3": "Yes. When creating a task you select one or several classes; the task is created for each of them.",
		"hb.faq.q4": "Does Praesto work on a phone?",
		"hb.faq.a4": "Yes, Praesto runs in the browser on computer, tablet and phone.",
		"hb.faq.q5": "In which languages is Praesto available?",
		"hb.faq.a5": "German, English, French and Italian. You can switch the language at the top right; your choice is remembered.",
		"hb.faq.q6": "Can we try Praesto first?",
		"hb.faq.a6": "Yes. With the live demo you can see a complete sample school (read-only) without setting anything up.",

		// 9 Support
		"hb.support.title": "Support & contact",
		"hb.support.p1": "Questions or requests? We're happy to help. Write to us at julia-peric@hotmail.com."
	},
	fr: {},
	it: {}
};
