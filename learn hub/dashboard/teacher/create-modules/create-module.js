$(document).ready(function() {

    var sidebar = $('#sidebar');
    
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
            var texte = $(this).val();
            if (texte != "") {
                alert("Recherche : " + texte);
            }
        }
    });

    $('#backBtn').click(function() {
        alert("Retour au tableau de bord");
    });

    $('#selectImageBtn').click(function() {
        $('#thumbnailInput').click();
    });

    $('#thumbnailInput').change(function() {
        var fichier = $(this).val();
        if (fichier != "") {
            alert("Image sélectionnée : " + fichier);
            $('.upload-text').text("Fichier chargé");
        }
    });

    $('#uploadArea').hover(function() {
        $(this).css('background-color', '#f9fafb');
        $(this).css('border-color', '#4a3bbf');
    }, function() {
        $(this).css('background-color', '');
        $(this).css('border-color', '');
    });

    $('#createModuleForm').submit(function(e) {
        e.preventDefault();
        
        var titre = $('#moduleTitle').val();
        var info = $('#moduleInfo').val();

        if (titre == "") {
            alert("Erreur : Le titre du module est obligatoire.");
        } else {
            alert("Le module '" + titre + "' a été créé avec succès.");
            $('#moduleTitle').val("");
            $('#moduleInfo').val("");
            $('.upload-text').text("Upload Thumbnail");
        }
    });

    $('#cancelBtn').click(function() {
        var confirmation = confirm("Voulez-vous vraiment annuler ? Les données seront perdues.");
        if (confirmation) {
            $('#moduleTitle').val("");
            $('#moduleInfo').val("");
            alert("Création annulée.");
        }
    });

    console.log("Page de création de module chargée");
});