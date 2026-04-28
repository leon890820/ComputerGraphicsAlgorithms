# ComputerGraphicsAlgorithms

一個使用 Java 與 OpenGL（LWJGL）實作的圖學專案，實現各種渲染技術與電腦圖學演算法。

---

## 專案簡介

**ComputerGraphicsAlgorithms** 是一個用來實作即時渲染（Real-time Rendering）與基礎圖學技術的小引擎。

本專案的目標是從底層開始理解並實作各種圖學演算法，包括光照模型、渲染流程，以及後續會實現近年相關論文的技術。

---

## 功能特色

* 即時 3D 渲染
* Blinn-Phong / Phong 光照模型
* 多種光源支援
  * 點光源（Point Light）
  * 平行光（Directional Light）
  * 聚光燈（Spot Light）
* 相機系統（WASD 移動 + 滑鼠視角控制）
* Scene Graph 架構
* 基本渲染流程（Rendering Pipeline）

---

## 預覽

https://github.com/user-attachments/assets/15a8351d-0556-430f-8454-9a976e9e92b7


---

## 操作

| 按鍵            | 功能   |
| ------------- | ---- |
| W / A / S / D | 移動相機 |
| 滑鼠右鍵拖曳        | 控制視角 |
| ESC           | 離開程式 |

---

## 執行方式


```bash
git clone https://github.com/your-username/GraphicsLab.git
cd ComputerGraphicsAlgorithms
```

使用 Gradle：

```bash
./gradlew run
```

或直接在 IntelliJ IDEA 中執行 `Main.java`。

---

## 相關文章

[👉 Computer Graphics Implementation Notes](https://hackmd.io/@leon890820/SycQsp-Xq)
