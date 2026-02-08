$(document).ready(function() {

    var sidebar = $('#adminSidebar');
    
    $('#mobileSidebarToggle').click(function() {
        if (sidebar.is(':visible')) {
            sidebar.hide();
        } else {
            sidebar.show();
        }
    });

    $('#notificationBtn').click(function() {
        var btn = $(this);
        btn.animate({ top: "-5px" }, "fast");
        btn.animate({ top: "5px" }, "fast");
        btn.animate({ top: "0px" }, "fast");
    });

    $('.search-bar input').keypress(function(e) {
        if (e.which == 13) {
            var valeur = $(this).val();
            if (valeur != "") {
                alert("Recherche globale : " + valeur);
            }
        }
    });

    $('#teacherSearch').keyup(function() {
        var recherche = $(this).val().toLowerCase();
        
        $('.teacher-row').each(function() {
            var ligne = $(this);
            var texte = ligne.text().toLowerCase();
            
            if (texte.indexOf(recherche) > -1) {
                ligne.show();
            } else {
                ligne.hide();
            }
        });
    });

    $('#filterBtn').click(function() {
        alert("Filtre des enseignants activé");
    });

    $('.delete-btn').click(function() {
        var confirmation = confirm("Voulez-vous supprimer cet enseignant ?");
        
        if (confirmation) {
            $(this).parents('tr').remove();
        }
    });

    $('.edit-btn').click(function() {
        var ligne = $(this).parents('tr');
        var nom = ligne.find('.teacher-name').text();
        alert("Modification du profil de : " + nom);
    });

    $('.pagination-btn').click(function() {
        alert("Fonctionnalité de pagination");
    });

    console.log("Gestion des enseignants chargée");
});