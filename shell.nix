{ pkgs ? (import "/home/cab/data/cab-nixpkgs/nixpkgs" {}) }:
with pkgs; mkShell {
  buildInputs = [androidStudioPackages.beta];
}


