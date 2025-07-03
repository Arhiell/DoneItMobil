using Microsoft.AspNetCore.Mvc;
using Microsoft.IdentityModel.Tokens;
using DoneItAPI.Data;
using DoneItAPI.Models;
using System.IdentityModel.Tokens.Jwt;
using System.Security.Claims;
using System.Text;
using Microsoft.EntityFrameworkCore;
using Microsoft.AspNetCore.Authorization;

namespace DoneItAPI.Controllers
{
    [ApiController]
    [Route("api/auth")]
    public class AuthController : ControllerBase
    {
        private readonly DoneItContext _context;
        private readonly IConfiguration _configuration;


        public AuthController(DoneItContext context, IConfiguration configuration)
        {
            _context = context;
            _configuration = configuration;
        }

        [HttpPost("login")]
        public async Task<IActionResult> Login([FromBody] LoginRequest request)
        {
            var usuario = await _context.Usuarios
                .FirstOrDefaultAsync(u => u.Email == request.Email || u.Nombre_Usuario == request.Email);

            if (usuario == null)
                return Unauthorized(new { error = "Email incorrecto." });

            if (request.Password != usuario.Password_Hash)
                return Unauthorized(new { error = "Contraseña incorrecta." });

            // Crear el token
            var tokenHandler = new JwtSecurityTokenHandler();
            var key = Encoding.ASCII.GetBytes(_configuration["Jwt:Secret"]);

            var tokenDescriptor = new SecurityTokenDescriptor
            {
                Subject = new ClaimsIdentity(new[]
                {
                new Claim(ClaimTypes.NameIdentifier, usuario.Id_Usuario.ToString()), 
                new Claim(ClaimTypes.Name, usuario.Nombre_Usuario)
                }),
                Expires = DateTime.UtcNow.AddHours(1),
                SigningCredentials = new SigningCredentials(
                    new SymmetricSecurityKey(key),
                    SecurityAlgorithms.HmacSha256Signature)
            };

            var token = tokenHandler.CreateToken(tokenDescriptor);
            var jwt = tokenHandler.WriteToken(token);

            return Ok(new { token = jwt });
        }

        [HttpPost("registro")]
        public async Task<IActionResult> Registro([FromBody] RegistroRequest request)
        {
            if (string.IsNullOrWhiteSpace(request.Email) ||
                string.IsNullOrWhiteSpace(request.Password) ||
                string.IsNullOrWhiteSpace(request.Nombre_Usuario))
            {
                return BadRequest(new { error = "Email, usuario y contraseña son obligatorios." });
            }

            if (await _context.Usuarios.AnyAsync(u => u.Email == request.Email))
                return Conflict(new { error = "Correo electrónico ya existe" });

            if (await _context.Usuarios.AnyAsync(u => u.Nombre_Usuario == request.Nombre_Usuario))
                return Conflict(new { error = "Nombre de usuario ya existe" });

            DateTime? fechaNacimiento = null;
            if (!string.IsNullOrWhiteSpace(request.Fecha_Nacimiento))
            {
                if (!DateTime.TryParse(request.Fecha_Nacimiento, out var parsedDate))
                    return BadRequest(new { error = "Formato de fecha inválido. Usa yyyy-MM-dd." });

                fechaNacimiento = parsedDate;
            }

            var nuevoUsuario = new Usuario
            {
                Nombre = request.Nombre,
                Apellido = request.Apellido,
                Email = request.Email,
                Nombre_Usuario = request.Nombre_Usuario,
                Fecha_Nacimiento = fechaNacimiento,
                Password_Hash = request.Password,
                Fecha_Registro = DateTime.UtcNow
            };

            _context.Usuarios.Add(nuevoUsuario);
            await _context.SaveChangesAsync();

            return Ok(new { mensaje = "Usuario creado con éxito" });
        }

        [HttpGet("perfil")]
        [Authorize]
        public IActionResult GetPerfil()
        {
            var userIdClaim = User.FindFirstValue(ClaimTypes.NameIdentifier);


            if (string.IsNullOrEmpty(userIdClaim))
                return Unauthorized(new { error = "No se pudo obtener el ID del usuario del token." });

            var userId = int.Parse(userIdClaim);
            var usuario = _context.Usuarios.FirstOrDefault(u => u.Id_Usuario == userId); // 🔧 CORREGIDO: Usuarios

            if (usuario == null) return NotFound();

            return Ok(new
            {
                nombre = usuario.Nombre,
                apellido = usuario.Apellido,
                nombre_usuario = usuario.Nombre_Usuario,
                email = usuario.Email,
                fecha_nacimiento = usuario.Fecha_Nacimiento?.ToString("yyyy-MM-dd"),
                fecha_registro = usuario.Fecha_Registro?.ToString("yyyy-MM-dd")
            });
        }
    }
}
