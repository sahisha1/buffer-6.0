# buffer-6.0
<h1 style="font-size:32px; font-weight:bold;">
  Project Title: GitStyleEditor – A Version-Controlled Text Editor
</h1>

## Description

GitStyleEditor is a **Java-based text editor** that supports **version control** and **user access management**. It allows multiple users to edit shared text files while maintaining a history of changes.

Key features include:
- Saving versions
- Viewing past edits
- Reverting to previous versions
- User authentication and access control

🔒 Only authorized users can view or modify files, making it suitable for general-purpose text editing and collaboration.

> **Note:** This editor currently focuses on plain text files. Features like syntax highlighting are **not included**.

---

## Key Features

- **User Login:** Authenticated access to files  
- **Access Control:** Role-based permissions for editing and viewing  
- **Save Version:** Commit changes with a message and timestamp  
- **View History:** Browse and restore previous versions  
- **Undo/Redo:** Navigate recent unsaved changes  
- **Contributor Logs:** Record who made which changes  

---

## Data Structures Used

- `List<String>` – Stores file content as a list of lines  
- `List<Snapshot>` – Keeps all committed versions with metadata  
- `Stack<List<String>>` – Used for undo and redo operations  
- `Map<String, List<LocalDateTime>>` – Tracks user contributions by time  
- `HashMap<String, Object>` – Stores snapshot metadata like message and author  
- `HashMap<String, User>` – Manages user accounts and roles  
- `Set<String>` – Stores allowed actions or roles per user  

---

## 📹 Video Link

[Click here to view the demo video](https://drive.google.com/drive/folders/1Ct0HIIQ1wlIxL3A3I2Rlzeij18joxI2B) 

---

## 📄 Report Link

[Open Project Report](https://docs.google.com/document/d/1aqGDsPayVlb25SRI40wRpDtJypMcqRgH2C4b6fA0APg/edit?tab=t.0#heading=h.gjr6v1uin22m) 

---
