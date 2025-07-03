using Microsoft.EntityFrameworkCore;
using DoneItAPI.Models;


namespace DoneItAPI.Data
{
    public class DoneItContext : DbContext
    {
        public DoneItContext(DbContextOptions<DoneItContext> options) : base(options) { }

        public DbSet<Usuario> Usuarios { get; set; }
        public DbSet<Proyecto> Proyectos { get; set; }
        public DbSet<Tarea> Tareas { get; set; }
        public DbSet<Usuario> Usuario { get; set; }


        // 🔽 Este método se encarga de configurar el "cascade delete"
        protected override void OnModelCreating(ModelBuilder modelBuilder)
        {
            modelBuilder.Entity<Proyecto>()
                .HasMany(p => p.Tareas)
                .WithOne()
                .HasForeignKey(t => t.Id_Proyecto)
                .OnDelete(DeleteBehavior.Cascade);
        }
    }
}
