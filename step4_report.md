# Step 4 Report  
### Testing Strategy • Known Bugs & Limitations • APK Build Process

## 1. Testing Strategy

Our project uses two types of tests:

### 1.1 Unit Tests (Logic-Level)
Located in `app/kotlin+java/com.example.honk/ReminderManagerTest`.

A small helper class `ReminderManager` was created to isolate reminder-related logic.  
Two unit tests verify:

- adding a reminder  
- deleting a reminder


![photo_5829985429774601232_x](https://github.com/user-attachments/assets/8a6309b6-4aaf-4988-921c-eec35f3677c5)
![photo_5829985429774601229_x](https://github.com/user-attachments/assets/1b13b6c1-4ede-4394-8755-0d1197d95b7e)
![photo_5829985429774601228_w](https://github.com/user-attachments/assets/0cf792ad-16cc-4d79-8101-78e2165e73cb)

These tests confirm that data manipulation works independently of UI/Android components.

### 1.2 UI Tests (Espresso)
Located in `app/kotlin+java/com.example.honk/AddReminderUITest`.

An Espresso test verifies the main user flow:

1. Launch `MainActivity`  
2. Open the “Add Reminder” dialog  
3. Enter text  
4. Submit  
5. Check that the new reminder appears


![photo_5829985429774601230_y](https://github.com/user-attachments/assets/505102c9-33e7-4ef0-a1ac-68192152895b)
![photo_5829985429774601227_y](https://github.com/user-attachments/assets/b409c7e8-55e2-4689-aae7-3d0da328a2d8)


This ensures that basic UI interaction, form submission, and list updates work correctly on the emulator.

---

## 2. Known Bugs & Limitations

- **Single image per reminder**: no support for multiple pictures or galleries.   
- **UI scaling differences** may appear on devices with very small or large screens.  
- **No full-size image viewer**: image preview cannot be enlarged.  
- **Minimal camera error handling**: only simple Toast messages on failure.
- **Database saving**: currently there's an ongoing bug with google.gms that limits out firestore DB usage possibilities.

---

## 3. APK Build Process

1. Launch Android Studio
2. Go to Build -> Generate Signed Bundle/APK
3. Choose APK
4. Create/Add your signing key
5. Choose build variant

This will produce a build of the app in the directory specified.

---

## 4. Summary

The testing strategy covers both logic (unit tests) and real user interaction (UI tests).  
The implemented tests validate the core reminder workflow and ensure stable behavior of the Add Reminder dialog.  
Current limitations mostly relate to prototype-level architecture and can be improved in the future iteration, but also a bug touches our DB infrastructure.