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

    $('#studentSearch').keyup(function() {
        var recherche = $(this).val().toLowerCase();
        
        $('.student-row').each(function() {
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
        alert("Ouverture du filtre avancé");
    });

    $('.delete-btn').click(function() {
        var confirmation = confirm("Êtes-vous sûr de vouloir supprimer cet étudiant ?");
        
        if (confirmation) {
            $(this).parents('tr').remove();
        }
    });

    $('.edit-btn').click(function() {
        var ligne = $(this).parents('tr');
        var nom = ligne.find('.student-name').text();
        alert("Édition du profil de : " + nom);
    });

    $('#addStudentBtn').click(function(e) {
        // Le lien href fonctionnera, mais on peut ajouter une alerte avant
        // alert("Redirection vers le formulaire d'inscription");
    });

    console.log("Gestion des étudiants chargée");
});