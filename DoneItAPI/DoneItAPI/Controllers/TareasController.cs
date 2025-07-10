using Microsoft.AspNetCore.Mvc;
using DoneItAPI.Models;
using DoneItAPI.DTOs;
using DoneItAPI.Data;
using Microsoft.EntityFrameworkCore;
using Microsoft.AspNetCore.Authorization;

namespace DoneItAPI.Controllers
{
    [ApiController]
    [Route("api/[controller]")]
    //[Authorize] // ← Descomentá si querés proteger con token
    public class TareasController : ControllerBase
    {
        private readonly DoneItContext _context;

        public TareasController(DoneItContext context)
        {
            _context = context;
        }

        [HttpPost]
        [HttpPost]
        public async Task<IActionResult> CrearTarea([FromBody] TareaRequest request)
        {
            try
            {
                var tarea = new Tarea
                {
                    Titulo = request.Titulo,
                    Descripcion = request.Descripcion,
                    Fecha_Inicio = request.Fecha_Inicio,
                    Fecha_Fin = request.Fecha_Fin,
                    Estado = request.Estado,
                    Prioridad = request.Prioridad,
                    Id_Proyecto = request.Id_Proyecto
                };

                _context.Tareas.Add(tarea);
                await _context.SaveChangesAsync();

                return Ok(new { mensaje = "Tarea creada con éxito", tarea.Id_Tarea });
            }
            catch (DbUpdateException ex)
            {
                // 🐞 Mostramos info detallada para debug
                return StatusCode(500, new
                {
                    error = "Error al guardar tarea",
                    detalle = ex.InnerException?.Message ?? ex.Message
                });
            }
            catch (Exception ex)
            {
                return StatusCode(500, new
                {
                    error = "Error inesperado",
                    detalle = ex.Message
                });
            }
        }


        [HttpGet("proyecto/{idProyecto}")]
        public async Task<IActionResult> GetTareasDeProyecto(int idProyecto)
        {
            var tareas = await _context.Tareas
                .Where(t => t.Id_Proyecto == idProyecto)
                .Select(t => new {
                    t.Id_Tarea,
                    t.Titulo,
                    t.Descripcion,
                    t.Fecha_Inicio,
                    t.Fecha_Fin,
                    t.Estado,
                    t.Prioridad,
                    t.Id_Proyecto
                })
                .ToListAsync();

            return Ok(tareas);
        }

        // ✅ NUEVO: DELETE /api/tareas/{id}
        [HttpDelete("{id}")]
        public async Task<IActionResult> EliminarTarea(int id)
        {
            var tarea = await _context.Tareas.FindAsync(id);
            if (tarea == null)
                return NotFound(new { error = "Tarea no encontrada" });

            _context.Tareas.Remove(tarea);
            await _context.SaveChangesAsync();

            return NoContent();
        }


        // PUT: api/tareas/{id}
        [HttpPut("{id}")]
        public async Task<IActionResult> EditarTarea(int id, [FromBody] EditarTareaRequest request)
        {
            var tareaExistente = await _context.Tareas.FindAsync(id);
            if (tareaExistente == null)
            {
                return NotFound();
            }

            // Solo actualizamos campos permitidos
            tareaExistente.Titulo = request.Titulo;
            tareaExistente.Descripcion = request.Descripcion;
            tareaExistente.Fecha_Inicio = request.Fecha_Inicio;
            tareaExistente.Fecha_Fin = request.Fecha_Fin;
            tareaExistente.Estado = request.Estado;
            tareaExistente.Prioridad = request.Prioridad;

            await _context.SaveChangesAsync();
            return NoContent();
        }


    }
}
