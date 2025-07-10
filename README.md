# 🚀 **DoneIt!** ❗

> 📌 *Gestor de tareas individuales y colaborativas orientado a la organización completa de proyectos.*

Una solución digital pensada para **crear, organizar, editar y compartir proyectos con múltiples tareas**, ideal para estudiantes, equipos de trabajo y uso personal. Incluye funcionalidades como manejo de estados, prioridades, fechas y generación de códigos QR para acceso rápido.

---
# 📱 [**DoneIt! - Versión Web**](https://github.com/Arhiell/DoneIt/tree/main)

🔗 **Este enlace en el título** te redirige al repositorio de la versión Web de DoneIt!.
---

## 🎯 Objetivo General

Desarrollar una aplicación completa con foco en **Desarrollo Web** y **Desarrollo Móvil**, integrando:

* Conocimientos adquiridos en ambas asignaturas.
* Patrones de arquitectura modernos (MVC, MVVM).
* Seguridad, escalabilidad y experiencia de usuario.

El objetivo es construir una herramienta realista y funcional que sirva como modelo de proyecto completo para el mundo profesional.

---

## 🛠️ Herramientas Web

| Herramienta            | Uso                                  |
| ---------------------- | ------------------------------------ |
| ⚙ ASP.NET Core MVC     | Backend estructurado con Razor Pages |
| 🔐 Identity + JWT      | Seguridad y autenticación            |
| 💾 MySQL               | Base de datos relacional             |
| 📦 EF Core + ADO.NET   | Acceso a datos ORM/directo           |
| 🎨 Bootstrap + CSS     | Estilos responsivos                  |
| 🖥️ Visual Studio 2022 | IDE de desarrollo                    |
| 📲 QRCoder (NuGet)     | Generación de códigos QR             |
| 🧠 JavaScript          | Dinámica en el frontend              |

---

## 📱 Herramientas Mobile

| Herramienta             | Uso                         |
| ----------------------- | --------------------------- |
| 🧩 Kotlin               | Lenguaje principal          |
| 📱 Android Studio       | IDE principal para mobile   |
| 🔄 Retrofit             | Consumo de APIs REST        |
| 📲 RecyclerView         | Listado de datos            |
| 🧠 ViewModel + LiveData | Arquitectura MVVM           |
| 🎨 XML Layouts          | Diseño visual de interfaces |

---

## 🧪 Modos de Uso

1. 🔨 Crear proyecto con un **nombre** y **descripción** editable.
2. 🧷 Agregar **una o varias tareas** al proyecto.
3. 📝 Cada tarea permite definir:

   * Nombre y descripción (editables).
   * Fecha de inicio y fin (modificables).
   * Estado: "En proceso", "Terminada", etc.
   * Prioridad: Alta, Media o Baja.
4. ❌ Se pueden eliminar proyectos y tareas.
5. 📷 Generar código QR para acceder a contenido (enlace, frase, etc.).

---

## 🎥 Demostraciones en video

| Funcionalidad | Enlace |
|---------------|--------|
| 📝 **Registro** | [Ver video](https://github.com/user-attachments/assets/9e4d29f6-c107-4981-9742-3e9e5c0e8b51) |
| 🔐 **Login** | [Ver video](https://github.com/user-attachments/assets/bf05413e-d071-4310-8df8-2590b7146fe6) |
| 👤 **Home → Perfil Usuario** | [Ver video](https://github.com/user-attachments/assets/6d1143d7-4a88-482c-a818-e53824a394c2) |
| 📂 **Mis Proyectos** | [Ver video](https://github.com/user-attachments/assets/f6bbc02e-8fad-455f-980f-097cce3ed47e) |
| ✏️🗑️ **Editar / Eliminar Proyecto** | [Ver video](https://github.com/user-attachments/assets/16a7cf26-e15e-447c-a30e-54ad450bbcdf) |
| 🆕 **Creación de Proyecto** | [Ver video](https://github.com/user-attachments/assets/4a4d5924-dbbe-49e4-86cc-b70158dfc92d) |
| 🔄 **Navegación con barra inferior** | [Ver video](https://github.com/user-attachments/assets/2da3e45e-c44d-47eb-bc4a-eb75edd25866) |

---

## 🛠️ Cambios Realizados

### 🔧 Mejoras generales
- 🧩 Se crearon **ViewModels** para cumplir correctamente con el patrón MVVM.
- 📥 En el **registro**, se reemplazó el botón *Enter* por *Siguiente* para facilitar el llenado de campos.
- 📧 Validación del correo electrónico: solo se permiten dominios válidos (`@gmail.com`, `@hotmail.com`, etc).

### 📅 Crear Proyecto
- ⚠️ Advertencia visual al intentar **eliminar una tarea**.
- 🕓 El formato de fecha fue ajustado a: `DD-MM-AAAA HH:MM` (antes incluía `T` y segundos).
- 🏷️ Se agregaron **títulos descriptivos** para los campos `Estado` y `Prioridad`.

### 🧭 Navegación
- 🚫 Todos los **spinners** ahora bloquean la **barra inferior de navegación** para evitar interferencias.
- ⬇️ En **Mis Proyectos**, ahora aparece un ícono `>` para **desplegar tareas**, que cambia a `<` para **ocultarlas**.
- ✏️ Al **editar un proyecto**, se mantienen las mismas precauciones que al crearlo:
  - ⚠️ Advertencia al eliminar tareas.
  - 💾 Spinner al guardar, que redirige automáticamente a *Mis Proyectos*.
- 🏠 Se agregó un **spinner** al volver a *Home* desde la vista de usuario.

---
## 👥 Autores

- 👨‍💻[**Ayala, Ariel.**](https://github.com/Arhiell)
- 👨‍💻[**Capovilla, Bautista.**](https://github.com/BautiC-9)
- 👨‍💻 [**Herro, Andres.**](https://github.com/HerreroAndre)
- 👨‍💻 [**Mill, Juan.**](https://github.com/r4ideny)

---
# 🎓 Datos Académicos
* 📚 **Materia:**Programación III
* 💻 **Carrera:** Tecnicatura Universitaria en Programación
* 🏫 **Institución:** **ITG** (Instituto Tecnológico Goya) – Sede **UTN** - Faculta Regional de Resistencia.
    * 📍 Extensión **Áulica Goya, Corrientes.**

* 📅 **Año:** 2025

* 👨‍🏫 **Profesores:** 
    - Fernandez, José. (Desarrollo Web).
    - Ramírez, Gerardo. (Desarrollo Mobil).



