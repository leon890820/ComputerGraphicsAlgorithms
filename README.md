# ComputerGraphicsAlgorithms

一個使用 Java 與 OpenGL（LWJGL）實作的圖學實驗專案，專注於各種渲染技術與電腦圖學演算法的探索。

---

## 🚀 專案介紹

**ComputerGraphicsAlgorithms** 是一個用來實驗與實作即時渲染（Real-time Rendering）與基礎圖學技術的個人專案。

本專案的目標是從底層開始理解並實作各種圖學演算法，包括光照模型、渲染流程，以及後續更進階的技術（如 Shadow Mapping、Ray Tracing 等）。

---

## ✨ 功能特色

* 即時 3D 渲染
* Blinn-Phong / Phong 光照模型
* 多種光源支援
  * 點光源（Point Light）
  * 平行光（Directional Light）
  * 聚光燈（Spot Light）
* 相機系統（WASD 移動 + 滑鼠視角控制）
* Scene Graph 架構
* 基本渲染流程（Rendering Pipeline）
* Deferred Rendering 架構（GBuffer，持續開發中）

---

## 🖼️ 預覽


---

## 🎮 操作方式

| 按鍵            | 功能   |
| ------------- | ---- |
| W / A / S / D | 移動相機 |
| 滑鼠右鍵拖曳        | 控制視角 |
| ESC           | 離開程式 |

---

## 🧱 專案結構

```
src/main/java/org/example/engine/
  core/        # 視窗與應用程式核心
  gameobject/  # 可渲染物件
  gl/          # OpenGL 封裝與工具
  light/       # 光源系統
  material/    # 材質與 shader
  math/        # 向量、矩陣與數學工具
  mesh/        # 模型載入
  render/      # 渲染核心
  renderPass/  # 渲染流程（如 GBuffer、Shadow 等）
  scene/       # 場景、相機、Transform

src/main/resources/
  meshes/      # 3D 模型資源
  textures/    # 貼圖資源
  shaders/     # GLSL shader
```

---

## 🛠️ 技術使用

* Java
* OpenGL（LWJGL）
* GLSL

---

## ▶️ 執行方式

### 1. 下載專案

```bash
git clone https://github.com/your-username/GraphicsLab.git
cd ComputerGraphicsAlgorithms
```

### 2. 執行專案

使用 Gradle：

```bash
./gradlew run
```

或直接在 IntelliJ IDEA 中執行 `Main.java`。

---

## 📖 技術文章

[👉 Computer Graphics Implementation Notes](https://hackmd.io/@leon890820/SycQsp-Xq)


---

A graphics lab exploring rendering techniques and computer graphics algorithms implemented in Java using OpenGL (LWJGL).

---

## 🚀 Overview

**ComputerGraphicsAlgorithms** is a personal project for experimenting with real-time rendering and fundamental computer graphics techniques.

The goal of this repository is to build and understand graphics algorithms from scratch, including lighting, rendering pipelines, and eventually more advanced topics like shadow mapping and ray tracing.

---

## ✨ Features

* Real-time 3D rendering
* Blinn-Phong / Phong shading
* Multiple light types

  * Point Light
  * Directional Light
  * Spot Light
* Camera system (WASD + mouse look)
* Scene graph structure
* Basic rendering pipeline
* Deferred rendering structure (GBuffer, in progress)

---

## 🖼️ Preview

> (Add screenshots here)

---

## 🎮 Controls

| Key              | Action      |
| ---------------- | ----------- |
| W / A / S / D    | Move camera |
| Right Mouse Drag | Look around |
| ESC              | Exit        |

---

## 🧱 Project Structure

```
src/main/java/org/example/engine/
  core/        # Window, application lifecycle
  gameobject/  # Renderable objects
  gl/          # OpenGL utilities & wrappers
  light/       # Lighting system
  material/    # Materials and shaders
  math/        # Vector / Matrix / math utilities
  mesh/        # Mesh loading
  render/      # Renderer core
  renderPass/  # Rendering pipeline stages
  scene/       # Scene, Camera, Transform

src/main/resources/
  meshes/      # 3D models
  textures/    # Texture assets
  shaders/     # GLSL shaders
```

---

## 🛠️ Tech Stack

* Java
* OpenGL (LWJGL)
* GLSL

---

## ▶️ Getting Started

### 1. Clone the repository

```bash
git clone https://github.com/your-username/GraphicsLab.git
cd ComputerGraphicsAlgorithms
```

### 2. Run the project

Using Gradle:

```bash
./gradlew run
```

Or run `Main.java` directly in IntelliJ IDEA.

---

## 📖 Article / Write-up

[👉 Computer Graphics Implementation Notes](https://hackmd.io/@leon890820/SycQsp-Xq)

---
