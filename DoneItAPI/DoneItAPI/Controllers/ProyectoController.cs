using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using DoneItAPI.Data;
using DoneItAPI.Models;
using Microsoft.EntityFrameworkCore;
using System.Security.Claims;

namespace DoneItAPI.Controllers
{
    [ApiController]
    [Route("api/proyectos")]
    [Authorize] // 🔒 Protegido: requiere token válido
    public class ProyectoController : ControllerBase
    {
        private readonly DoneItContext _context;

        public ProyectoController(DoneItContext context)
        {
            _context = context;
        }

        // 🔹 GET /proyectos - trae todos los proyectos del usuario autenticado
        [HttpGet]
        public async Task<IActionResult> GetProyectosDelUsuario()
        {
            var userIdClaim = User.FindFirst(ClaimTypes.NameIdentifier);
            if (userIdClaim == null)
                return Unauthorized(new { error = "Token inválido" });

            int idUsuario = int.Parse(userIdClaim.Value);

            var proyectos = await _context.Proyectos
                .Where(p => p.Id_Usuario == idUsuario)
                .ToListAsync();

            return Ok(proyectos);
        }

        // 🔹 GET /proyectos/{id} - trae un proyecto por ID si pertenece al usuario
        [HttpGet("{id}")]
        public async Task<IActionResult> GetProyectoPorId(int id)
        {
            var userIdClaim = User.FindFirst(ClaimTypes.NameIdentifier);
            if (userIdClaim == null)
                return Unauthorized(new { error = "Token inválido" });

            int idUsuario = int.Parse(userIdClaim.Value);

            var proyecto = await _context.Proyectos
                .FirstOrDefaultAsync(p => p.Id_Proyecto == id && p.Id_Usuario == idUsuario);

            if (proyecto == null)
                return NotFound(new { error = "Proyecto no encontrado" });

            return Ok(proyecto);
        }

        // 🔹 POST /proyectos - crea un nuevo proyecto
        [HttpPost]
        public async Task<IActionResult> CrearProyecto([FromBody] ProyectoRequest request)
        {
            var userIdClaim = User.FindFirst(ClaimTypes.NameIdentifier);

            if (userIdClaim == null)
                return Unauthorized(new { error = "Token inválido" });

            int idUsuario = int.Parse(userIdClaim.Value);

            var proyecto = new Proyecto
            {
                Nombre = request.Nombre,
                Descripcion = request.Descripcion,
                Fecha_Creacion = DateTime.Now,
                Id_Usuario = idUsuario
            };

            _context.Proyectos.Add(proyecto);
            await _context.SaveChangesAsync();

            return Ok(new { idProyecto = proyecto.Id_Proyecto });
        }

        // 🔹 PUT /proyectos/{id} - actualiza un proyecto existente
        [HttpPut("{id}")]
        public async Task<IActionResult> EditarProyecto(int id, [FromBody] ProyectoRequest request)
        {
            var userIdClaim = User.FindFirst(ClaimTypes.NameIdentifier);
            if (userIdClaim == null)
                return Unauthorized(new { error = "Token inválido" });

            int idUsuario = int.Parse(userIdClaim.Value);

            var proyecto = await _context.Proyectos
                .FirstOrDefaultAsync(p => p.Id_Proyecto == id && p.Id_Usuario == idUsuario);

            if (proyecto == null)
                return NotFound(new { error = "Proyecto no encontrado" });

            proyecto.Nombre = request.Nombre;
            proyecto.Descripcion = request.Descripcion;

            await _context.SaveChangesAsync();

            return Ok(new { mensaje = "Proyecto actualizado" });
        }

        // ✅ DELETE /proyectos/{id} - elimina un proyecto existente
        [HttpDelete("{id}")]
        public async Task<IActionResult> EliminarProyecto(int id)
        {
            var userIdClaim = User.FindFirst(ClaimTypes.NameIdentifier);
            if (userIdClaim == null)
                return Unauthorized(new { error = "Token inválido" });

            int idUsuario = int.Parse(userIdClaim.Value);

            var proyecto = await _context.Proyectos
                .FirstOrDefaultAsync(p => p.Id_Proyecto == id && p.Id_Usuario == idUsuario);

            if (proyecto == null)
                return NotFound(new { error = "Proyecto no encontrado" });

            // 🔥 Eliminar las tareas asociadas primero
            var tareas = await _context.Tareas
                .Where(t => t.Id_Proyecto == id)
                .ToListAsync();

            _context.Tareas.RemoveRange(tareas);
            _context.Proyectos.Remove(proyecto);
            await _context.SaveChangesAsync();

            return Ok(new { mensaje = "Proyecto y tareas eliminados con éxito" });
        }

    }
}
